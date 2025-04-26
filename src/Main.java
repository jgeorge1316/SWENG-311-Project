import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        System.out.println("Enter 1 for Create Account, Enter 2 for Log-in.");
        Scanner input = new Scanner(System.in);

        while (true) {
            int userInput = input.nextInt();


            if(userInput == 1) {


            }
            else if(userInput == 2) {


            }
            else{
                System.out.println("Invalid Input");
        }

            String MasterPass = "";


            while (true) {
                System.out.println("Enter your master password: ");
                String inputPass = input.nextLine();
                if (inputPass.equals("Pass")) {
                    break;
                }
            }


        }


    }
}