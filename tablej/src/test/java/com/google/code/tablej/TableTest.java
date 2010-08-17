package com.google.code.tablej;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

public class TableTest {

	Table<Cat> cats;

	@Before
	public void setUp() {

		cats = new Table<Cat>();

		cats.index("names", new Indexer<Cat>() {

			@Override
			protected Object[] getValues(final Cat t) {
				return new Object[] { t.getName() };
			}

		});

		cats.index("age_and_male", new Indexer<Cat>() {

			@Override
			protected Object[] getValues(final Cat t) {
				return new Object[] { t.getAge(), t.isMale() };
			}

		});

	}

	public static class Cat {

		private final String name;
		private final int age;
		private final boolean male;

		public Cat(final String name, final int age, final boolean male) {
			super();
			this.name = name;
			this.age = age;
			this.male = male;
		}

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}

		public boolean isMale() {
			return male;
		}

		@Override
		public String toString() {
			return "Cat [name=" + name + ", male=" + male + ", age=" + age + "]";
		}

	}

	@Test
	public void testFind() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		final Cat vasya = new Cat("vasya", 10, true);
		final Cat markiz = new Cat("markiz", 10, true);

		cats.add(vasya);
		cats.add(markiz);
		cats.add(new Cat("noname", 3, false));

		final Collection<Cat> vasyas = cats.find("names", "vasya");

		assertTrue(vasyas.contains(vasya));
		assertEquals(1, vasyas.size());

		final Collection<Cat> tenYearsOldMales = cats.find("age_and_male", 10, true);

		assertTrue(tenYearsOldMales.contains(vasya));
		assertTrue(tenYearsOldMales.contains(markiz));
		assertEquals(2, tenYearsOldMales.size());
	}

	@Test
	public void testRemove() throws Exception {

		final Cat vasya = new Cat("vasya", 10, true);
		final Cat markiz = new Cat("markiz", 10, true);
		final Cat kitten = new Cat("noname", 3, false);

		cats.add(vasya);
		cats.add(markiz);
		cats.add(kitten);

		cats.remove(markiz);

		cats.remove("names", "noname");

		assertEquals(1, cats.size());

	}
}
