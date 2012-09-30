
/**
 * @author Riccardo Baral
 * 
 */

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
 
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

 
class EnergiesparAppServer {
	
    private static final String DATABASE_NAME = "AppDatenbank.db";
    public static final String DATABASE_TABLE_METERREADINGS = "meterReadings";
    public static final String DATABASE_TABLE_CHANGES = "veraenderungsTabelle";
    
    private static int port = 7676; /* port the server listens on */
 
    public static void main (String[] args) throws IOException, Exception {
    	
    	// Öffnen der lokalen Datenbank auf dem Server
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:AppDatenbank.db");
        Statement stat = conn.createStatement();
        
        ArrayList<String> statements = new ArrayList<String>();
        
//        stat.executeUpdate("create table people (name, occupation);");
//        PreparedStatement prep = conn.prepareStatement("insert into people values (?, ?);");
//     
//        prep.setString(1, "Gandhi");
//        prep.setString(2, "politics");
//        prep.addBatch();
//     
//        prep.setString(1, "Turing");
//        prep.setString(2, "computers");
//        prep.addBatch();
//     
//        prep.setString(1, "Wittgenstein");
//        prep.setString(2, "smartypants");
//        prep.addBatch();
//     
//        conn.setAutoCommit(false);
//        prep.executeBatch();
//        conn.setAutoCommit(true);
//     
//        ResultSet rs = stat.executeQuery("select * from people;");
//        while (rs.next())
//        {
//          System.out.println("name = " + rs.getString("name"));
//          System.out.println("job = " + rs.getString("occupation"));
//        }
//        rs.close();

        
    	
        ServerSocket server = null;
        try {
            server = new ServerSocket(port); /* start listening on the port */
        	System.out.println("* Starte Server: " + server.getLocalSocketAddress());
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            System.err.println(e);
            System.exit(1);
        }
 
        // Endlosschleife
        while (true)
        {
	        Socket client = null;
	        try {
	            client = server.accept();
	        } catch (IOException e) {
	            System.err.println("Accept failed.");
	            System.err.println(e);
	            System.exit(1);
	        }
	 
	        /* obtain an input stream to the client */
	        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
	 
	        PrintWriter out = null;
	        out = new PrintWriter(client.getOutputStream(), true);
	        
	        String msg;
	        
	    	// TODO: datenbankzugriff
	        String letztesDatum = "1348953933670";
	        
			Calendar letzteSync = Calendar.getInstance();
			letzteSync.setTimeInMillis(Long.parseLong(letztesDatum));
	        
	        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	        String letzteSyncString = format.format(letzteSync.getTime());
			
	        System.out.println("* Ein Klient hat sich erfolgreich verbunden.");
	    	System.out.println("* Sende Datum der letzten Synchronisation: " + letzteSyncString);
	    	System.out.println("* Warten auf Übertragung...");
	    	
	        out.println(letztesDatum);
	        
	        /* loop reading lines from the client and display them */
	        while ((msg = in.readLine()) != null) {
	        	if (msg.compareTo("END") != 0)
	        	{
	        		System.out.println(">> " + msg);
	        		statements.add(msg);
	        	}
	        	else
	        	{
	        		System.out.println("* Ende der Übertragung");
	        		System.out.println("* Es wurden "+String.valueOf(statements.size())+" Einträge übernommen.");
	        		
	        		try {
	        			
						for (int i = 0; i < statements.size(); i++)
						{
						    stat.executeUpdate(statements.get(i));
						}
						
						statements.clear();
		                
					} catch (SQLException e) {
						System.out.println("* " + e.toString());
					}
	
	        		// else end
	
	        	}
	        	
	        	// while-loop end
	        }
	        
	        System.out.println("* Der Klient hat die Verbindung getrennt.");
	    }
        
        // TODO:wann? wann nicht?
        // conn.close();
    }
}