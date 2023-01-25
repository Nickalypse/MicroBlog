

public class Report {
	
	private final String username;
	private final ReportType type;
	
	// COSTRUTTORE
	public Report(String username, ReportType type) throws NullPointerException {
		if(username == null || type == null) throw new NullPointerException();
		this.username = username;
		this.type = type;
	}
	
	
	public String getUsername() {
		return this.username;
	}
	
	public ReportType getType() {
		return this.type;
	}
	
}
