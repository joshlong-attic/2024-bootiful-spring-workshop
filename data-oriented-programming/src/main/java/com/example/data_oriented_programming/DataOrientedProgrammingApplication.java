package com.example.data_oriented_programming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataOrientedProgrammingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataOrientedProgrammingApplication.class, args);
    }

}

class Loans {
    
    String displayMessageFor(Loan loan) {
        return switch (loan) {
            case UnsecuredLoan(var interest) -> "ouch! that " + interest + "% rate is going to hurt!";
            case SecuredLoan sl -> "good job! nice loan.";
        };
    }
}

sealed interface Loan permits SecuredLoan, UnsecuredLoan {
}

final class SecuredLoan implements Loan {
}

record UnsecuredLoan(float interest) implements Loan {
}