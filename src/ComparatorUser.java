import java.util.Comparator;


	public class ComparatorUser implements Comparator<PersonModel> {

		@Override
		public int compare(PersonModel lhs, PersonModel rhs) {
			PersonModel mBean1 = lhs;
			PersonModel mBean2 = rhs;
			if (mBean1.exp > mBean2.exp) {
				return 0;
			} else {
				return 1;
			}
		}
	}