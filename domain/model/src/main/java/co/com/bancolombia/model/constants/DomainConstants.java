package co.com.bancolombia.model.constants;

public class DomainConstants {

    private DomainConstants() {}

    public static final String APPLICATION_CANNOT_BE_NULL = "Application cannot be null";
    public static final String EMAIL_CANNOT_BE_NULL_OR_EMPTY = "Email cannot be null or empty";
    public static final String AMOUNT_MUST_BE_GREATER_THAN_ZERO = "Amount must be greater than zero";
    public static final String LOAN_TYPE_ID_CANNOT_BE_NULL = "Loan type ID cannot be null";
    public static final String USER_DOESNT_EXIST = "User doesn't exist!";

    public static final String UNKNOWN_STATUS = "Unknown";
    public static final String PENDING_REVIEW_LABEL = "Pending review";
    public static final String REJECTED_LABEL = "Rejected";
    public static final String MANUAL_REVIEW_LABEL = "Manual review";
    public static final String APPROVED_LABEL = "Approved";

    public static final String UNKNOWN_USER_NAME = "Unknown User";
    public static final String NOT_AVAILABLE = "N/A";

    public static final String PENDING_REVIEW = "pending review";
    public static final String REJECTED = "rejected";
    public static final String MANUAL_REVIEW = "manual review";
    public static final String APPROVED = "approved";

    public static final int PENDING_REVIEW_ID = 1;
    public static final int REJECTED_ID = 2;
    public static final int MANUAL_REVIEW_ID = 3;
    public static final int APPROVED_ID = 4;
}