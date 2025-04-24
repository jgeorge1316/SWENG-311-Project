public class test1 {
    public static void main(String[] args){
        int currentUserId = 123;

        // Assuming you have a list of accounts:
        Vector<Account> allAccounts = new Vector<>();
        allAccounts.add(new Account(1, currentUserId, "myemail", "pass123", "https://gmail.com", "work email"));
        allAccounts.add(new SharedAccount(new Vector<>(List.of(123, 456)), "shareduser", "sharedpass", "https://netflix.com", "family account"));

        Vector<Account> accessible = getAccountsForUser(currentUserId, allAccounts);

        for (Account acc : accessible) {
            System.out.println(acc);
        }

    }
}
