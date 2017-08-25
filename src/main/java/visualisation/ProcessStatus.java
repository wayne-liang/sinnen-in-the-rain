package visualisation;

public enum ProcessStatus {
	COMPLETED,
	STOPPED,
	INPROGRESS;


	@Override
	public String toString() {
		switch(this) {
		case COMPLETED: return "Completed";
		case STOPPED: return "Stopped";
		case INPROGRESS: return "In Progress";
		default: throw new IllegalArgumentException();
		}
	}
}
