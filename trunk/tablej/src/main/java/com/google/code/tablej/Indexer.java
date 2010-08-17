package com.google.code.tablej;

import java.util.Arrays;

/**
 * Callback class, used by Table<T>, to build indexes. See {@link Table}
 * 
 * @author shaman
 * 
 * @param <T>
 */
public abstract class Indexer<T> {

	static final class ID {

		private final int hashCode;

		ID(final Object[] objects) {
			this.hashCode = Arrays.hashCode(objects);
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final ID other = (ID) obj;
			if (hashCode != other.hashCode) {
				return false;
			}
			return true;
		}

	}

	/**
	 * Implementation must return an array of values, extracted from T instance,
	 * by simply calling some methods of t. <b>Attention! Be careful!</b> All
	 * objects returned here <b>MUST</b> implement hashCode().
	 * 
	 * @param t
	 * @return
	 */
	protected abstract Object[] getValues(T t);

	final ID getID(final T t) {
		return new ID(getValues(t));
	}

	static ID getID(final Object... objects) {
		return new ID(objects);
	}
}
