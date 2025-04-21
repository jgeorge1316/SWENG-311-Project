public class Main {
    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.println("Enter your master password: ");
            String inputPass = input.nextLine();
            if (inputPass.equals("Pass")) {
                break;
            }
        }
            System.out.println("Enter 1 for Create Account, 2 for search for account, 3 for account info.");
            int userInput = input.nextInt();
    }
}