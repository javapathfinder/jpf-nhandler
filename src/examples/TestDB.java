public class TestDB {
    public static void main(String[] a)	throws Exception {
    	MyDB.createConnection();
    	MyDB.createTable();
    	MyDB.insertAddress(1245, "Lakeside dr.");
    	MyDB.insertAddress(300, "Bloor street East");
    	MyDB.shutdown();
    }
}