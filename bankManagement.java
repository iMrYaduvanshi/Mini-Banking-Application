package Mini_Banking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;

public class bankManagement {
    private static final int NULL = 0;
    static Connection con = null; // Initialize to null
    static String sql = "";

    // Method to initialize the database connection
    public static void initializeConnection() {
        con = connection.getConnection();
    }

    public static int getAccountNumber(String username) {
        int accountNumber = -1; // default to -1 to indicate not found
        try {
            sql = "select ac_no from customer where cname=?";
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                accountNumber = rs.getInt("ac_no");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accountNumber;
    }

    public static boolean createAccount(String name, int passCode) {
        try {
            if (name.isEmpty() || passCode == NULL) {
                System.out.println("All Field Required!");
                return false;
            }
            Statement st = con.createStatement();
            sql = "INSERT INTO customer(cname,balance,pass_code) values('" + name + "',1000," + passCode + ")";

            if (st.executeUpdate(sql) == 1) {
                System.out.println(name + ", Now You Login!");
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Username Not Available!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean loginAccount(String name, int passCode) {
        try {
            if (name.isEmpty() || passCode == NULL) {
                System.out.println("All Field Required!");
                return false;
            }
            sql = "select * from customer where cname='" + name + "' and pass_code=" + passCode;
            PreparedStatement st = con.prepareStatement(sql);
            ResultSet rs = st.executeQuery();

            BufferedReader sc = new BufferedReader(new InputStreamReader(System.in));

            if (rs.next()) {
                int ch = 5;
                int amt = 0;
                int senderAc = rs.getInt("ac_no");
                int receiveAc;
                while (true) {
                    try {
                        System.out.println("Hallo, " + rs.getString("cname"));
                        System.out.println("1)Transfer Money");
                        System.out.println("2)View Balance");
                        System.out.println("3)Deposit Money");
                        System.out.println("5)LogOut");

                        System.out.print("Enter Choice:");
                        ch = Integer.parseInt(sc.readLine());
                        if (ch == 1) {
                            System.out.print("Enter Receiver  A/c No:");
                            receiveAc = Integer.parseInt(sc.readLine());
                            System.out.print("Enter Amount:");
                            amt = Integer.parseInt(sc.readLine());

                            if (bankManagement.transferMoney(senderAc, receiveAc, amt)) {
                                System.out.println("MSG : Money Sent Successfully!\n");
                            } else {
                                System.out.println("ERR :  Failed!\n");
                            }
                        } else if (ch == 2) {
                            bankManagement.getBalance(senderAc);
                        }
                        else if(ch==3){
                            System.out.print("Enter Amount:");
                            amt = Integer.parseInt(sc.readLine());
                            bankManagement.AddMoney(senderAc,amt);
                        }else if (ch == 5) {
                            break;
                        } else {
                            System.out.println("Err : Enter Valid input!\n");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                return false;
            }
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Username Not Available!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void getBalance(int acNo) {
        try {
            sql = "select * from customer where ac_no=" + acNo;
            PreparedStatement st = con.prepareStatement(sql);
            ResultSet rs = st.executeQuery(sql);
            System.out.println("-----------------------------------------------------------");
            System.out.printf("%12s %10s %10s\n", "Account No", "Name", "Balance");

            while (rs.next()) {
                System.out.printf("%12d %10s %10d.00\n", rs.getInt("ac_no"), rs.getString("cname"), rs.getInt("balance"));
            }
            System.out.println("-----------------------------------------------------------\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean transferMoney(int sender_ac, int receiver_ac, int amount) throws SQLException {
        if (receiver_ac == NULL || amount == NULL) {
            System.out.println("All Field Required!");
            return false;
        }
        try {
            con.setAutoCommit(false);
            sql = "select * from customer where ac_no=" + sender_ac;
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                if (rs.getInt("balance") < amount) {
                    System.out.println("Insufficient Balance!");
                    return false;
                }
            }

            Statement st = con.createStatement();
            con.setSavepoint();

            sql = "update customer set balance=balance-" + amount + " where ac_no=" + sender_ac;
            if (st.executeUpdate(sql) == 1) {
                System.out.println("Amount Debited!");
            }

            sql = "update customer set balance=balance+" + amount + " where ac_no=" + receiver_ac;
            st.executeUpdate(sql);

            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            con.rollback();
        }
        return false;
    }
    public static boolean AddMoney(int sender_ac,int amount) throws SQLException {
        if (amount == NULL) {
            System.out.println("Field Required!");
            return false;
        }
        try {
            con.setAutoCommit(false);
//            sql = "select * from customer where ac_no=" + sender_ac;
//            PreparedStatement ps = con.prepareStatement(sql);
//            ResultSet rs = ps.executeQuery();
//
//            if (rs.next()) {
//                if (rs.getInt("balance") < amount) {
//                    System.out.println("Insufficient Balance!");
//                    return false;
//                }
//            }

            Statement st = con.createStatement();
            con.setSavepoint();

            sql = "update customer set balance=balance+" + amount + " where ac_no=" + sender_ac;
            if (st.executeUpdate(sql) == 1) {
                System.out.println("Amount Credited!");
            }

//            sql = "update customer set balance=balance+" + amount + " where ac_no=" + receiver_ac;
//            st.executeUpdate(sql);

            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            con.rollback();
        }
        return false;
    }
}