import java.util.*;

public class Store {
    private String onoma;
    private String katigoria;
    private double platos; 
    private double mhkos;  
    private int asteria;
    private int arithmospsifwn;
    private Map<String, Product> proionta; //key=onoma proiontos, value=Product object

    // constructor pou arxikopoiei ola ta pedia
    public Store(String onoma, String katigoria, double platos, double mhkos, int asteria, int arithmospsifwn) {
        this.onoma = onoma;
        this.katigoria = katigoria;
        this.platos = platos;
        this.mhkos = mhkos;
        this.asteria = asteria;
        this.arithmospsifwn = arithmospsifwn;
        this.proionta = new HashMap<>();

    }
    //epistrefei thn kathgoria timhs me bash ton meso oro timwn twn diathesimwn proiontwn
    public String getAveragePriceCategoryOfAvailableProducts() {
        double sunolo = 0;
        int count = 0;

        for (Product product : proionta.values()) {
            if (product.getAvailableAmount() > 0) {
                sunolo += product.getPrice();
                count++;
            }
        }

        //an den yparxei proion me diathesimo apothema
        if (count == 0) return "$"; 

        double mesosOros = sunolo / count;

        if (mesosOros <= 5) {
            return "$";
        } else if (mesosOros <= 15) {
            return "$$";
        } else {
            return "$$$";
        }
    }

    //getters gia MapReduce
    public String getName() {
        return onoma;
    }

    public String getCategory() {
        return katigoria;
    }

    public double getLatitude() {
        return platos;
    }

    public double getLongitude() {
        return mhkos;
    }

    public int getStars() {
        return asteria;
    }

    public int getNoOfVotes() {
        return arithmospsifwn;
    }


    public void addProduct(String onomaproiontos, double timi, int posothta) {
        proionta.put(onomaproiontos, new Product(onomaproiontos, timi, posothta));
    }

    //afairei proion apo store, epistrefei true an yphrxe kai afairethike
    public synchronized boolean removeProduct(String onomaproiontos) {
        if (proionta.containsKey(onomaproiontos)) {
            proionta.remove(onomaproiontos);
            return true;
        }
        return false;
    }

    //allazei to apothema enos proiontos
    public void updateStock(String onomaproiontos, int neaposothta) {
        if (proionta.containsKey(onomaproiontos)) {
            proionta.get(onomaproiontos).setAvailableAmount(neaposothta);
        }
    }
    public synchronized String addRating(float asteria) {
        if (asteria < 1 || asteria > 5) {
            return "LATHOS VATHMOLOGIA. PREPEI NA EINAI APO 1 EOS 5.";
        }
        //to ksanaupologizei 
        asteria = Math.round(((this.asteria * arithmospsifwn + asteria) / (arithmospsifwn + 1)) * 10) / 10.0f;
        arithmospsifwn++;
        return "TO KATASTHMA VATHMOLOGITHIKE. NEO M.O: " + asteria + " (" + arithmospsifwn + " VATHMOLOGIES)";
    }
    
    //epistrefei lista proiontwn ws string
    public StringBuilder listProducts() {
        if (proionta.isEmpty()) {
            return new StringBuilder("DEN UPARXOUN PORIONTA SE AUTO TO KATASTHMA.");
        }

        StringBuilder sb = new StringBuilder("PROIONTA:\n");
        for (Product p : proionta.values()) {
            sb.append(p).append("\n");
        }
        return sb;
    }


    //agora proiontos me sygkekrimenh posothta
    public synchronized boolean buyProduct(String onomaproiontos, int posothta) {
        if (proionta.containsKey(onomaproiontos)) {
            Product proion = proionta.get(onomaproiontos);
            int diathesimothta = proion.getAvailableAmount();

            if (diathesimothta >= posothta) {
                proion.setAvailableAmount(diathesimothta - posothta);
                proion.increaseSalesCount(posothta);
                return true;
            } else {
                //oxi arketi posothta
                return false; 
            }
        }
        //den yparxei to proion
        return false; 
    }

    //epistrefei report me sunolikes pwlhseis ana proion
    public String getSalesReport() {
        StringBuilder sb = new StringBuilder("ANALUTIKO REPORT PWLHSEWN:\n");
        for (Product p : proionta.values()) {
            sb.append(p.getName()).append(": ")
              .append(p.getSalesCount()).append(" TEMAXIA\n");
        }
        return sb.toString();
    }
}
