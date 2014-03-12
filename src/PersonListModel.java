import java.util.ArrayList;

public class PersonListModel {

	public int result;
	public String message;
	public ArrayList<PersonModel> data;

	@Override
	public String toString() {
		return "PersonListModel [result=" + result + ", message=" + message
				+ ", data=" + data + "]";
	}

}
