package com.cx.thunder.util;
import java.util.Comparator;

import com.cx.thunder.model.PersonModel;


	/**
	 * 降序排列
	 * @author CX
	 *
	 */
	public class ComparatorUser implements Comparator<PersonModel> {

		@Override
		public int compare(PersonModel lhs, PersonModel rhs) {
			if (lhs.exp > rhs.exp) {
				return -1;
			} else {
				return 1;
			}
		}
	}