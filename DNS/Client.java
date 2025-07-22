import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

class Client {
    public static void main(String a[]) {
        Socket clisock = null;
        DataInputStream input = null;
        PrintStream ps = null;
        String url, ip = "", s, u, p, str;
        int pno = 5123;
        Connection con = null;
        Statement smt = null;
        ResultSet rs;
        boolean status = true;
        try {
            System.out.println("Enter name to resolve (e.g., www.example.com):");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            url = br.readLine();

            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3307/dns", "root", "");
            smt = con.createStatement();

            while (status) {
                s = "";
                System.out.println("IN CLIENT URL IS: " + url);
                StringTokenizer st = new StringTokenizer(url, ".");

                if (st.countTokens() == 1) {
                    status = false;
                }

                while (st.countTokens() > 1) {
                    s = s + st.nextToken() + ".";
                }
                if (!s.isEmpty()) {
                    s = s.substring(0, s.length() - 1).trim();
                }

                u = st.nextToken();
                System.out.println("u=" + u);

                rs = smt.executeQuery("select port,ipadd from client where name='" + u + "'");
                if (rs.next()) {
                    p = rs.getString(1);
                    pno = Integer.parseInt(p);
                    str = rs.getString(2);
                    url = s;
                    if (ip.isEmpty()) {
                        ip = str;
                    } else {
                        ip = str + "." + ip;
                    }
                } else {
                    System.out.println("pno=" + pno);
                    clisock = new Socket("127.0.0.1", pno);
                    input = new DataInputStream(clisock.getInputStream());
                    ps = new PrintStream(clisock.getOutputStream());
                    ps.println(url);

                    p = input.readLine();
                    if (p.startsWith("Illegal address")) {
                        System.out.println(p);
                        status = false;
                        break;
                    }
                    pno = Integer.parseInt(p);

                    str = input.readLine();
                    url = input.readLine();

                    if (ip.isEmpty()) {
                        ip = str;
                    } else {
                        ip = str + "." + ip;
                    }

                    smt.executeUpdate("insert into client values('" + u + "','" + str + "','" + p + "')");
                }
                System.out.println("Current Resolved IP part: " + ip);
            }

            if (!ip.isEmpty() && ip.endsWith(".")) {
                ip = ip.substring(0, ip.length() - 1).trim();
            }
            System.out.println("\nFinal IP address is: " + ip);

        } catch (ConnectException ce) {
            System.err.println("Connection refused. Make sure the appropriate server is running on port " + pno);
        } catch (SocketException se) {
            System.err.println("Network error: " + se.getMessage());
        } catch (NumberFormatException nfe) {
            System.err.println("Error parsing port number. Received non-numeric data from server.");
        } catch (Exception e) {
            System.err.println("Client Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) ps.close();
                if (input != null) input.close();
                if (clisock != null) clisock.close();
                if (smt != null) smt.close();
                if (con != null) con.close();
            } catch (IOException ioe) {
                System.err.println("Client I/O Closing Error: " + ioe.getMessage());
            } catch (SQLException se) {
                System.err.println("Client SQL Closing Error: " + se.getMessage());
            }
        }
    }
}