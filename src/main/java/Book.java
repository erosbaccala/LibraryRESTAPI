public class Book {
    private String Autore,Titolo,ISBN;
    private int Prezzo;

    public String getAutore() {
        return Autore;
    }

    public void setAutore(String autore) {
        Autore = autore;
    }

    public String getTitolo() {
        return Titolo;
    }

    public void setTitolo(String titolo) {
        Titolo = titolo;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public int getPrezzo(){
        return Prezzo;
    }

    public void setPrezzo(int Prezzo){
        this.Prezzo = Prezzo;
    }
}
