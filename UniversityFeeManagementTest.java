import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class SimpleUniversityFeeTest {
    
    public static void main(String[] args) {
       
        Instant now = Instant.now();
        Instant pastDeadline = now.minusSeconds(86400); 
        Instant futureDeadline = now.plusSeconds(86400); 
        
        
        Student student = new Student(1, "Dupont", "Jean", now.minusSeconds(31536000));
        
     
        Fee lateFee = new Fee(1, "Frais en retard", 500.0, pastDeadline, student);
        Fee paidFee = new Fee(2, "Frais payé", 300.0, pastDeadline, student);
        Fee futureFee = new Fee(3, "Frais futur", 400.0, futureDeadline, student);
        
       
        lateFee.addPayment(new CashPayment(1, 200.0, now.minusSeconds(43200))); 
        paidFee.addPayment(new CashPayment(2, 300.0, now.minusSeconds(43200))); 
        
        List<Fee> fees = Arrays.asList(lateFee, paidFee, futureFee);
        
     
        System.out.println("Frais en retard: " + lateFee.getStatusAtTime(now));
        System.out.println("Frais payé: " + paidFee.getStatusAtTime(now));
        System.out.println("Frais futur (non payé): " + futureFee.getStatusAtTime(now));
        
        
        System.out.println("Frais en retard: " + Statistics.getLateFees(fees, now).size() + " trouvé(s)");
        System.out.println("Total manquant: " + Statistics.getTotalMissingFees(fees, now) + " €");
        System.out.println("Total payé par étudiant: " + Statistics.getTotalPaidByStudent(student, fees, now) + " €");
        

        Fee overpaidFee = new Fee(4, "Frais surpayé", 100.0, pastDeadline, student);
        overpaidFee.addPayment(new CashPayment(3, 150.0, now.minusSeconds(43200)));
        System.out.println("Frais surpayé: " + overpaidFee.getStatusAtTime(now));
    }
}
