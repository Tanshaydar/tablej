package com.google.code.tablej;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import com.google.code.tablej.Indexer.ID;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

public class Table<T> extends AbstractCollection<T> {

	final static class Link {

	}

	// added once, so does not have to be thread safe
	private final Map<String, Indexer<T>> indexers = new HashMap<String, Indexer<T>>();
	final ReferenceQueue<Link> rq = new ReferenceQueue<Link>();

	final HashMap<String, HashMultimap<Indexer.ID, WeakReference<Link>>> indexes = new HashMap<String, HashMultimap<Indexer.ID, WeakReference<Link>>>();
	private final BiMap<Link, T> values; // thread safe

	private final Runnable cleanup = new Runnable() {

		private void cleanup(final Reference<? extends Link> reference) {

			if (null == reference) {
				return;
			}

			final Collection<HashMultimap<ID, WeakReference<Link>>> ndexes = indexes.values();

			for (final HashMultimap<ID, WeakReference<Link>> index : ndexes) {
				synchronized (index) {
					final Collection<WeakReference<Link>> references = index.values();

					references.remove(reference);
				}

			}
		}

		public void run() {
			while (true) {
				try {
					cleanup(rq.remove());
				} catch (final InterruptedException e) {

				}
			}
		}
	};

	private final Thread thread;

	// TODO remove thread form this object
	public Table() {
		final BiMap<Link, T> biMap = HashBiMap.create();

		values = Maps.synchronizedBiMap(biMap);

		thread = new Thread(cleanup, "Table cleanupper");
		thread.start();
	}

	/**
	 * Builds a named index, based on indexer (see {@link Indexer}) function.
	 * 
	 * @param name
	 * @param indexer
	 */
	public void index(final String name, final Indexer<T> indexer) {
		final HashMultimap<Indexer.ID, WeakReference<Link>> index = HashMultimap.create();
		indexes.put(name, index);
		indexers.put(name, indexer);

	}

	@Override
	public boolean add(final T t) {
		final Set<Entry<String, Indexer<T>>> entrySet = indexers.entrySet();

		final Link link = new Link();
		final WeakReference<Link> weakReference = new WeakReference<Link>(link, rq);

		values.put(link, t);

		for (final Entry<String, Indexer<T>> entry : entrySet) {
			final String name = entry.getKey();
			final Indexer<T> indexer = entry.getValue();

			final Indexer.ID id = indexer.getID(t);
			final HashMultimap<Indexer.ID, WeakReference<Link>> index = indexes.get(name);

			synchronized (index) {
				index.put(id, weakReference);
			}

		}
		return true;
	}

	/**
	 * Finds values using given index name and parameter values. For instance,
	 * you may call it this way:
	 * 
	 * <pre>
	 * find("logins", login);
	 * ...
	 * or
	 * ...
	 * find("role_and_office", manager, currentOffice);
	 * </pre>
	 * 
	 * @param indexName
	 * @param params
	 * @return
	 */
	public synchronized Collection<T> find(final String indexName, final Object... params) {
		final HashMultimap<Indexer.ID, WeakReference<Link>> index = indexes.get(indexName);

		final Indexer.ID id = Indexer.getID(params);

		final Collection<WeakReference<Link>> references;

		synchronized (index) {
			references = index.get(id);

			final Collection<T> result = new ArrayList<T>();

			for (final WeakReference<Link> reference : references) {

				final Link key = reference.get();

				if (null == key) {
					continue;
				}

				final T addition = values.get(key);

				if (null == addition) {
					continue;
				}

				result.add(addition);
			}
			return result;
		}

	}

	public synchronized T findFirst(final String indexName, final Object... params) {

		final Collection<T> found = find(indexName, params);

		if (found.isEmpty()) {
			return null;
		}

		final T t = Iterators.get(found.iterator(), 0);

		return t;
	}

	/**
	 * Performs search, just like find() and removes all found objects.
	 * 
	 * @param indexName
	 * @param params
	 */
	public synchronized void remove(final String indexName, final Object... params) {
		final Collection<T> toRemove = find(indexName, params);

		synchronized (values) {
			Iterators.removeAll(iterator(), toRemove);
		}
	}

	@Override
	public Iterator<T> iterator() {
		return values.values().iterator();
	}

	@Override
	public int size() {
		return values.size();
	}

}
