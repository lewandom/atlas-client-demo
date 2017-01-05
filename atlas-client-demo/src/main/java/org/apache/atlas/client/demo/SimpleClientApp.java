package org.apache.atlas.client.demo;

import static org.apache.atlas.client.demo.Utils.WAIT;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.typesystem.Referenceable;

public class SimpleClientApp implements Runnable {

    private static final String[] ATLAS_URLS = new String[] { "http://localhost:21000" };

    private static final String ATLAS_USERNAME = "admin";

    private static final String ATLAS_PASSWORD = "admin";

    private static final int ATLAS_WAIT_PERIOD = 5000; // 5s

    private static final String TYPE_DBTABLE = "DbTable";

    private static final String TYPE_DBCOLUMN = "DbColumn";

    private AtlasClient client;

    public static void main(String[] args) {
        new SimpleClientApp().run();
    }

    @Override
    public void run() {
        try {
            client = new AtlasClient(ATLAS_URLS, new String[] { ATLAS_USERNAME, ATLAS_PASSWORD });

            do {
                System.out.println("Atlas status: " + client.getAdminStatus());
            } while (!client.isServerReady() && WAIT.apply(ATLAS_WAIT_PERIOD));

            createOrUpdateTypes();

            Referenceable table1 = new Referenceable(TYPE_DBTABLE);
            table1.getValuesMap().put("name", "table1");
            table1.getValuesMap().put("qualifiedName", "table1");
            List<Referenceable> table1Columns = new ArrayList<>();
            for (int i = 0; i < 3; ++i) {
                Referenceable column = new Referenceable(TYPE_DBCOLUMN);
                column.getValuesMap().put("name", "column" + i);
                column.getValuesMap().put("qualifiedName", "table1.column" + i);
                column.getValuesMap().put("type", "VARCHAR");
                table1Columns.add(column);
            }
            table1.getValuesMap().put("columns", table1Columns);
            client.createEntity(table1);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createOrUpdateTypes() throws AtlasServiceException, IOException {
        String dbTypesJson = readDbTypesJson();
        List<String> types = client.listTypes();
        if (!types.contains(TYPE_DBCOLUMN)) {
            List<String> createdTypes = client.createType(dbTypesJson);
            System.out.println("Created: " + createdTypes);
        } else {
            // NOTE: Atlas does not support:
            // - deleting types
            // - updating types by adding attributes with multiplicity == REQUIRED
            List<String> updatedTypes = client.updateType(dbTypesJson);
            System.out.println("Updated: " + updatedTypes);
        }
    }

    private String readDbTypesJson() throws IOException {
        InputStream is = getClass().getResourceAsStream("/dbtypes.json");
        InputStreamReader reader = new InputStreamReader(is, "UTF-8");
        try {
            StringBuilder sb = new StringBuilder();
            int count;
            char[] cbuf = new char[8192];
            while ((count = reader.read(cbuf)) > 0) {
                sb.append(cbuf, 0, count);
            }
            return sb.toString();
        } finally {
            reader.close();
        }
    }

}
