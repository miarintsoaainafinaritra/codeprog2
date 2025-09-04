import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


enum FeeStatus {
    IN_PROGRESS,
    PAID,
    LATE,
    NULL,
    OVERPAID
}


abstract class Payment {
    protected int id;
    protected double amount;
    protected Instant paymentDateTime;
    
    public Payment(int id, double amount, Instant paymentDateTime) {
        this.id = id;
        this.amount = amount;
        this.paymentDateTime = paymentDateTime;
    }
    
    public int getId() { return id; }
    public double getAmount() { return amount; }
    public Instant getPaymentDateTime() { return paymentDateTime; }
}


class CashPayment extends Payment {
    public CashPayment(int id, double amount, Instant paymentDateTime) {
        super(id, amount, paymentDateTime);
    }
}

class CreditCardPayment extends Payment {
    private String cardNumber;
    
    public CreditCardPayment(int id, double amount, Instant paymentDateTime, String cardNumber) {
        super(id, amount, paymentDateTime);
        this.cardNumber = cardNumber;
    }
    
    public String getCardNumber() { return cardNumber; }
}

class BankTransferPayment extends Payment {
    private String bankAccount;
    
    public BankTransferPayment(int id, double amount, Instant paymentDateTime, String bankAccount) {
        super(id, amount, paymentDateTime);
        this.bankAccount = bankAccount;
    }
    
    public String getBankAccount() { return bankAccount; }
}


class Group {
    private int id;
    private String name;
    
    public Group(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Group group && id == group.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


class Teacher {
    private int id;
    private String lastName;
    private String firstName;
    
    public Teacher(int id, String lastName, String firstName) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
    }
    
    public int getId() { return id; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
}


class Student {
    private int id;
    private String lastName;
    private String firstName;
    private Instant entryDate;
    private List<GroupHistory> groupHistory;
    
    public Student(int id, String lastName, String firstName, Instant entryDate) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.entryDate = entryDate;
        this.groupHistory = new ArrayList<>();
    }
    
    public int getId() { return id; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public Instant getEntryDate() { return entryDate; }
    public List<GroupHistory> getGroupHistory() { return groupHistory; }
    
    public void addGroupHistory(Group group, Instant joinDate) {
        groupHistory.add(new GroupHistory(group, joinDate));
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Student student && id == student.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
   
    public static class GroupHistory {
        private Group group;
        private Instant joinDate;
        
        public GroupHistory(Group group, Instant joinDate) {
            this.group = group;
            this.joinDate = joinDate;
        }
        
        public Group getGroup() { return group; }
        public Instant getJoinDate() { return joinDate; }
    }
}


class Fee {
    private int id;
    private String label;
    private double amountToPay;
    private Instant deadline;
    private Student student;
    private List<Payment> payments;
    
    public Fee(int id, String label, double amountToPay, Instant deadline, Student student) {
        this.id = id;
        this.label = label;
        this.amountToPay = amountToPay;
        this.deadline = deadline;
        this.student = student;
        this.payments = new ArrayList<>();
    }
    
    public int getId() { return id; }
    public String getLabel() { return label; }
    public double getAmountToPay() { return amountToPay; }
    public Instant getDeadline() { return deadline; }
    public Student getStudent() { return student; }
    public List<Payment> getPayments() { return payments; }
    
    public void addPayment(Payment payment) {
        payments.add(payment);
    }
    
    public double getTotalPaid() {
        return payments.stream().mapToDouble(Payment::getAmount).sum();
    }
    
    public double getTotalPaidAtTime(Instant time) {
        return payments.stream()
                .filter(payment -> !payment.getPaymentDateTime().isAfter(time))
                .mapToDouble(Payment::getAmount)
                .sum();
    }
    
   
    public FeeStatus getStatusAtTime(Instant time) {
        double totalPaid = getTotalPaidAtTime(time);
        
        if (totalPaid == 0) {
            return FeeStatus.NULL;
        }
        
        if (totalPaid > amountToPay) {
            return FeeStatus.OVERPAID;
        }
        
        if (totalPaid == amountToPay) {
            return FeeStatus.PAID;
        }
        
       
        if (time.isAfter(deadline)) {
            return FeeStatus.LATE;
        } else {
            return FeeStatus.IN_PROGRESS;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Fee fee && id == fee.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


class Statistics {
    
    public static List<Fee> getLateFees(List<Fee> fees, Instant t) {
        return fees.stream()
                .filter(fee -> fee.getStatusAtTime(t) == FeeStatus.LATE)
                .collect(Collectors.toList());
    }
    
    
    public static double getTotalMissingFees(List<Fee> fees, Instant t) {
        return fees.stream()
                .filter(fee -> fee.getStatusAtTime(t) == FeeStatus.LATE)
                .mapToDouble(fee -> fee.getAmountToPay() - fee.getTotalPaidAtTime(t))
                .sum();
    }
    
   
    public static double getTotalPaidByStudent(Student student, List<Fee> fees, Instant t) {
        return fees.stream()
                .filter(fee -> fee.getStudent().equals(student))
                .mapToDouble(fee -> fee.getTotalPaidAtTime(t))
                .sum();
    }
}


class UniversityFeeManagementTest {
    
    public static void main(String[] args) {
      
        Instant now = Instant.now();
        Instant pastDeadline = now.minusSeconds(86400);
        Instant futureDeadline = now.plusSeconds(86400); 
        Instant paymentTime1 = now.minusSeconds(43200); 
        Instant paymentTime2 = now.minusSeconds(21600); 
        
      
        Student student1 = new Student(1, "Dupont", "Jean", now.minusSeconds(31536000));
        Student student2 = new Student(2, "Martin", "Marie", now.minusSeconds(15768000)); 
        
      
        Group group1 = new Group(1, "L3 INFO");
        Group group2 = new Group(2, "M1 INFO");
        
       
        student1.addGroupHistory(group1, now.minusSeconds(31536000));
        student2.addGroupHistory(group2, now.minusSeconds(15768000));
        
       
        Fee fee1 = new Fee(1, "Frais de scolarité S1", 1000.0, pastDeadline, student1);
        Fee fee2 = new Fee(2, "Frais de scolarité S2", 1200.0, futureDeadline, student1);
        Fee fee3 = new Fee(3, "Frais de bibliothèque", 50.0, pastDeadline, student2);
        
        
        Payment payment1 = new CashPayment(1, 500.0, paymentTime1);
        Payment payment2 = new CreditCardPayment(2, 300.0, paymentTime2, "1234-5678-9012-3456");
        Payment payment3 = new BankTransferPayment(3, 60.0, paymentTime1, "FR76 1234 5678 9012 3456 7890 123");
        
       
        fee1.addPayment(payment1); 
        fee2.addPayment(payment2); 
        fee3.addPayment(payment3); 
        
        List<Fee> allFees = Arrays.asList(fee1, fee2, fee3);
        
       
        System.out.println("Fee 1 status: " + fee1.getStatusAtTime(now)); 
      
        
     
        Fee fee4 = new Fee(4, "Frais test", 100.0, futureDeadline, student1);
        System.out.println("Fee 4 status (no payments): " + fee4.getStatusAtTime(now)); 

        List<Fee> lateFees = Statistics.getLateFees(allFees, now);
        System.out.println("Late fees count: " + lateFees.size()); 
      
        double totalMissing = Statistics.getTotalMissingFees(allFees, now);
        System.out.println("Total missing fees: " + totalMissing);
        
        double totalPaidStudent1 = Statistics.getTotalPaidByStudent(student1, allFees, now);
      
        System.out.println("Total paid by student 1: " + totalPaidStudent1); 
        
        Fee fee5 = new Fee(5, "Frais exact", 100.0, futureDeadline, student1);
        Payment payment4 = new CashPayment(4, 100.0, paymentTime1);
        fee5.addPayment(payment4);
        System.out.println("Fee 5 status (exact payment): " + fee5.getStatusAtTime(now));
        
        
        
    }
}
public class UniversityFeeManagement {
    public static void main(String[] args) {
        UniversityFeeManagementTest.main(args);
    }
}
