
public class StoreFactory {

    public static Store fromJson(String json) {
        try {
             //katharizoume ta \n, \r, tabs gia na exoume mia grammiki morfi tou json
            json = json.replaceAll("[\\n\\r\\t]", "").trim();

            //pairnoume ta stoixeia tou store me xrhsh twn methodwn extract
            String onoma = extractString(json, "\"StoreName\"");
            double platos = extractDouble(json, "\"Latitude\"");
            double mhkos = extractDouble(json, "\"Longitude\"");
            String katigoria = extractString(json, "\"FoodCategory\"");
            int stars = extractInt(json, "\"Stars\"");
            int arithmosPsifwn = extractInt(json, "\"NoOfVotes\"");
            //ftiaxnoume neo Store me ta parapanw stoixeia
            Store store = new Store(onoma, katigoria, platos, mhkos, stars, arithmosPsifwn);

            //anazitisi pinaka products
            String productsSection = json.split("\"Products\"\\s*:\\s*\\[")[1].split("]")[0];
            //spame ta products se chunks me vasi to "," metaksy twn antikeimenwn
            String[] productChunks = productsSection.split("\\},\\s*\\{");
            //xrisimopoieitai gia upologismo mesou orou timwn
            double synolikoKostos = 0;
            for (String raw : productChunks) {
                String pJson = raw;
                //katharizoume brackets an exoun meinei
                if (!pJson.startsWith("{")) {
                    pJson = "{" + pJson;
                }
                if (!pJson.endsWith("}")){
                    pJson = pJson + "}";
                }

                String onomaProiontos = extractString(pJson, "\"ProductName\"");
                double kostos = extractDouble(pJson, "\"Price\"");
                int posotita = extractInt(pJson, "\"Available Amount\"");
                //prosthetoume to product sto store
                store.addProduct(onomaProiontos, kostos, posotita);
                synolikoKostos += kostos;
            }
            return store;

        } catch (Exception e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
            return null;
        }
    }

    //methodos gia string
    private static String extractString(String json, String key) {
        int arxi = json.indexOf(key);
        if (arxi == -1) return "";
        int sthlh = json.indexOf(":", arxi);
        int enarksiFrashs = json.indexOf("\"", sthlh + 1);
        int liksiFrashs = json.indexOf("\"", enarksiFrashs + 1);
        return json.substring(enarksiFrashs + 1, liksiFrashs);
    }

    //methodos gia double
    private static double extractDouble(String json, String key) {
        int arxi = json.indexOf(key);
        if (arxi == -1) return 0;
        int sthlh = json.indexOf(":", arxi);
        String arithmos = json.substring(sthlh + 1).split("[,}]")[0].trim();
        return Double.parseDouble(arithmos);
    }

    //methodos gia int
    private static int extractInt(String json, String key) {
        return (int) extractDouble(json, key);
    }
}
