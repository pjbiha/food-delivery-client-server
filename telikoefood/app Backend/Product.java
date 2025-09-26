public class Product {
    private String onoma;
    private double kostos;
    private int diathesimiPosotita;
    //arithmos sunolikwn pwlisewn gia to proion
    private int salesCount = 0; 

    //constructor pou arxikopoiei ta pedia tou proiontos
    public Product(String onoma, double kostos, int diathesimiPosotita) {
        this.onoma = onoma;
        this.kostos = kostos;
        this.diathesimiPosotita = diathesimiPosotita;
    }

    public String getName() {
        return onoma;
    }
    //epistrefei timh proiontos 
    public double getPrice() {
        return kostos;
    }
    
    //epistrefei to diathesimo apothema tou proiontos
    public int getAvailableAmount() {
        return diathesimiPosotita;
    }

    //allazei to apothema tou proiontos
    public void setAvailableAmount(int amount) {
        this.diathesimiPosotita = amount;
    }

    //auksanei ton arithmo pwlisewn
    public void increaseSalesCount(int quantity) {
        this.salesCount += quantity;
    }

    //epistrefei poses fores exei pwlhthei to proion
    public int getSalesCount() {
        return salesCount;
    }

    //epistrefei string me plirofories tou proiontos
    public String toString() {
        return onoma + " | Price: " + kostos+ " euro | Stock: " + diathesimiPosotita;
    }
}
