package game.ai.rl;

import game.AbstractAction;
import game.AbstractBoard;
import game.AbstractColor;
import game.AbstractPosition;
import game.Game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A standard Q-Table, storing values in a Map.
 *  
 * @author Federico Ferri
 *
 * @param <S> class representing a board (the state)
 * @param <A> class representing an action
 * @param <C> class representing a color
 * @param <P> class representing a position
 */
public class QTable<S extends AbstractBoard<P, A, C>, A extends AbstractAction<P>, C extends AbstractColor, P extends AbstractPosition> extends AbstractQTable<S, A, C, P> {
	private Map<QTableKey<S, A>, Double> q;
	
	public QTable() {
		q = new LinkedHashMap<QTableKey<S, A>, Double>();
	}
	
	@Override
	public double get(S state, A action) {
		QTableKey<S, A> key = new QTableKey<S, A>(state, action);
		if(q.containsKey(key)) {
			return q.get(key);
		} else {
			return 0;
		}
	}
	
	@Override
	public void set(S state, A action, double value) {
		q.put(new QTableKey<S, A>(state, action), value);
	}
	
	public int size() {
		return q.size();
	}
	
	public void removeFirst() {
		Iterator<QTableKey<S, A>> i = q.keySet().iterator();
		if(!i.hasNext()) return;
		i.next();
		i.remove();
	}

	@Override
	public void load(File f, Game<S, A, C, P> game) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(f));
		load(r, game);
		r.close();
	}
	
	@Override
	public void load(BufferedReader r, Game<S, A, C, P> game) throws IOException {
		S dummy = game.getInitialState();
		q = new HashMap<QTableKey<S, A>, Double>();
		String s, t[];
		s = r.readLine();
		if(s == null) return;
		int numLines = Integer.parseInt(s);
		for(int i = 0; i < numLines && (s = r.readLine()) != null; i++) {
			t = s.split("=");
			String t2[] = t[0].split(":");
			S state = game.stateFromString(t2[0]);
			A action = dummy.actionFromString(t2[1]);
			QTableKey<S, A> k = new QTableKey<S, A>(state, action);
			Double v = Double.parseDouble(t[1]);
			q.put(k, v);
		}
	}
	
	@Override
	public void save(File f, Game<S, A, C, P> game) throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
		save(w, game);
		w.close();
	}
	
	@Override
	public void save(BufferedWriter w, Game<S, A, C, P> game) throws IOException {
		S dummy = game.getInitialState();
		w.write(q.size() + "\n");
		for(Map.Entry<QTableKey<S, A>, Double> e : q.entrySet()) {
			String strState = game.stateToString(e.getKey().getState());
			String strAction = dummy.actionToString(e.getKey().getAction());
			w.write(strState + ":" + strAction + "=" + e.getValue() + "\n");
		}
	}
	
	public void dump(boolean sort, int limit) {
		List<Map.Entry<QTableKey<S, A>, Double>> l = new ArrayList<>();
		l.addAll(q.entrySet());
		if(sort)
			Collections.sort(l, new Comparator<Map.Entry<QTableKey<S, A>, Double>>() {
				public int compare(Entry<QTableKey<S, A>, Double> o1, Entry<QTableKey<S, A>, Double> o2) {
					return o1.getValue().compareTo(o2.getValue());
				}
			});
		for(Map.Entry<QTableKey<S, A>, Double> e : l) {
			if(limit-- < 0) break;
			System.out.println("state:\n" + e.getKey().getState() + "action: " + e.getKey().getAction() + " value: " + e.getValue());
		}
	}
}
