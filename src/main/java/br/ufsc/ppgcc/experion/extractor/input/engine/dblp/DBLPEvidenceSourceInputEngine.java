package br.ufsc.ppgcc.experion.extractor.input.engine.dblp;

import br.ufsc.ppgcc.experion.Experion;
import br.ufsc.ppgcc.experion.extractor.evidence.PhysicalEvidence;
import br.ufsc.ppgcc.experion.extractor.input.BaseSourceInputEngine;
import br.ufsc.ppgcc.experion.extractor.input.EvidenceSourceInput;
import br.ufsc.ppgcc.experion.extractor.input.engine.technique.ExtractionTechnique;
import br.ufsc.ppgcc.experion.extractor.input.engine.technique.LDAExtractionTechnique;
import br.ufsc.ppgcc.experion.extractor.input.engine.technique.LDAExtractionTechniqueTFIDF;
import br.ufsc.ppgcc.experion.extractor.input.engine.technique.TFIDFExtractionTechnique;
import br.ufsc.ppgcc.experion.model.expert.Expert;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.sql.*;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class DBLPEvidenceSourceInputEngine extends BaseSourceInputEngine implements Serializable {

    public static class LDA extends DBLPEvidenceSourceInputEngine {
        public LDA() throws SQLException, ClassNotFoundException {
            super(new LDAExtractionTechnique(), true);
        }
    }

    public static class LDATFIDF extends DBLPEvidenceSourceInputEngine {
        public LDATFIDF() throws SQLException, ClassNotFoundException {
            super(new LDAExtractionTechniqueTFIDF(), true);
        }
    }

    public static class TFIDF extends DBLPEvidenceSourceInputEngine {
        public TFIDF() throws SQLException, ClassNotFoundException {
            super(new TFIDFExtractionTechnique(), true);
        }
    }

    public static class KeyGraph extends DBLPEvidenceSourceInputEngine {
        public KeyGraph() throws SQLException, ClassNotFoundException {
            super(new KeygraphExtractionTechniqueDBLP(), true);
        }
    }

    public static class KeyGraphTFIDF extends DBLPEvidenceSourceInputEngine {
        public KeyGraphTFIDF() throws SQLException, ClassNotFoundException {
            super(new KeygraphExtractionTechniqueDBLPTFIDF(), true);
        }
    }

    private transient Connection connection;

    public DBLPEvidenceSourceInputEngine() throws SQLException, ClassNotFoundException {
        this(null, false);
    }

    public DBLPEvidenceSourceInputEngine(ExtractionTechnique extractionTechnique) throws SQLException, ClassNotFoundException {
        this(extractionTechnique, false);
    }

    public DBLPEvidenceSourceInputEngine(ExtractionTechnique extractionTechnique, boolean connectToDatabase) throws SQLException, ClassNotFoundException {
        setExtractionTechnique(extractionTechnique);
        if (connectToDatabase) {
            this.connectToDatabase();
        }
    }

    public void connectToDatabase() throws SQLException, ClassNotFoundException {
        if (connection == null) {
            String url = "jdbc:postgresql://%s:%d/%s?charSet=UNICODE";
            Class.forName("org.postgresql.Driver");
            url = url.format(url, Experion.getInstance().getConfig().getString("dblp.db.host"), Experion.getInstance().getConfig().getInt("dblp.db.port"),
                    Experion.getInstance().getConfig().getString("dblp.db.database"));
            connection = DriverManager.getConnection(url, Experion.getInstance().getConfig().getString("dblp.db.user"), Experion.getInstance().getConfig().getString("dblp.db.password"));
        }
    }

    public void disconnectDatabase() throws SQLException {
        connection.close();
    }

    @Override
    public Set<Expert> getExpertEntities() {
        try {
            this.connectToDatabase();
        Set<Expert> entities = new HashSet<>();
        Statement st = null;
        st = connection.createStatement();
        ResultSet rs = st.executeQuery("select id, name from author_map order by id asc");
        while (rs.next()) {
            Expert expert = new Expert(rs.getString(1), rs.getString(2));
            expert.setIdentification(rs.getString(1));
            expert.setName(rs.getString(2));
            entities.add(expert);
        }
        rs.close();
        return entities;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<PhysicalEvidence> getNewEvidences(Expert expert, EvidenceSourceInput input) {
        String idInSource = expert.getIdentificationForSource(this.getEvidenceSource());
        try {
            this.connectToDatabase();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
        PreparedStatement st = null;
        Set<PhysicalEvidence> evidences = new HashSet<>();
        try {
            st = connection.prepareStatement("select d.year, d.title, '' from author_map am inner join author_map_author ama on (ama.author_map_id=am.id) inner join author_document ad on (ad.author_id=ama.author_id) inner join document d on (d.id=ad.document_id) where am.id = CAST(? AS INTEGER) and d.type <> 'www' order by d.year");
            st.setString(1, idInSource);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                PhysicalEvidence evidence = new PhysicalEvidence();
                evidence.setExpert(expert);
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.MONTH, 0);
                cal.set(Calendar.YEAR, rs.getInt(1));
                evidence.setTimestamp(cal.getTime());

                String keywords = "";
                if (!StringUtils.isBlank(rs.getString(2))) {
                    keywords += rs.getString(2);
                }
                if (!StringUtils.isBlank(rs.getString(3))) {
                    keywords += " " + rs.getString(3);
                }
                keywords = keywords.trim();
                if (!StringUtils.isBlank(keywords)) {
                    evidence.addKeywords(keywords.split(" "));
                    evidence.setInput(input);
                    evidences.add(evidence);
                }


                EvidenceSourceURLDBLP url = new EvidenceSourceURLDBLP();
                url.setUrl("DBLP Record");
                url.setRetrievedData(String.format("Ano: %s\nTítulo: %s", rs.getString(1), rs.getString(2)));
                evidence.setUrl(url);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        evidences = customizeEvidences(expert, evidences);

        return evidences;
    }

    protected Set<PhysicalEvidence> customizeEvidences(Expert expert, Set<PhysicalEvidence> evidences) {
        return this.getExtractionTechnique().generateEvidences(expert, evidences, this.getLanguage());
    }

    @Override
    public Set<Expert> findExpertByName(String name) {
        try {
            this.connectToDatabase();
        Set<Expert> entities = new HashSet<>();
        PreparedStatement st = null;
        st = connection.prepareStatement("select id, name from author_map where lower(name) like ? order by id asc");
        st.setString(1, "%" + name.toLowerCase() + "%");
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            Expert expert = new Expert(rs.getString(1), rs.getString(2));
            expert.setIdentification(rs.getString(1));
            expert.setName(rs.getString(2));
            entities.add(expert);
        }
        rs.close();
        return entities;
        } catch (ClassNotFoundException|SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
