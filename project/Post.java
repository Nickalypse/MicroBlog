import java.util.Date;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


public class Post implements Cloneable {
	
	/**
		OVERVIEW:	Tipo di dato modificabile che rappresenta un post testuale univoco creato da uno specifico autore
					e che raccoglie i nomi delle persone che hanno messo like
		
		ELEMENTO TIPICO:	<id_univoco, autore, testo, data_pubblicazione, data_ultima_modifica, {persona_0, ..., persona_n}>
							con persona_i ≠ null
		
		AF =	<this.id, this.autore, this.text, this.data_pub, this.data_mod, this.likes>
				{persona_0, ..., persona_n} diventa la lista this.likes senza ripetizioni e con valori non nulli
		
		RI =	(id ≠ null) && (autore ≠ null) && (text ≠ null) && (likes ≠ null)	&&
				(count ≥ 0) && (data_mod ≥ data_pub) && (0 < text.length() ≤ 140)	&&
				(post(count)=pre(count) || post(count)=pre(count)+1)				&&
				(post(data_mod) ≥ pre(data_mod)) && (data_pub > 0)					&&
				
				(FOR ALL i . 0 ≤ i < likes.size()
					likes.get(i) ≠ null												&&
					autore ≠ likes.get(i) )											&&
				
				(FOR ALL i,j . 0 ≤ i < j < likes.size()
					likes.get(i) ≠ likes.get(j) )
	 **/
	
	private static final int MAX_LENGTH = 140;	// costante con totale massimo di caratteri consentiti per il testo del post
	private static long count = 0;				// contatore totale di post creati
	
	private final long id;						// identificatore univoco del post (non riassegnabile)
	private final String autore;				// autore del post (non riassegnabile)
	private String text;						// messaggio testuale del post (riassegnabile dopo modifiche)
	private final long data_pub;				// data e ora di creazione del post (non riassegnabile)
	private long data_mod;						// data e ora di ultima modifica del testo del post (riassegnabile dopo modifiche)
	private final ArrayList<String> likes;		// lista degli utenti che hanno messo like al post (non riassegnabile)
	
	
	// COSTRUTTORE
	public Post(String autore, String text) throws NullPointerException, IllegalTextException {
		if(autore == null || text == null) throw new NullPointerException();
		if(text.length() == 0 || text.length() > MAX_LENGTH) throw new IllegalTextException();
		
		this.id = getNextCount();
		this.autore = autore;
		this.text = text;
		this.data_pub = new Date().getTime();
		this.data_mod = this.data_pub;
		this.likes = new ArrayList<String>();
	}
	// REQUIRES:	autore ≠ null, text ≠ null, 0 < text.length() ≤ Post.MAX_LENGTH
	// THROWS:		se autore = null || text = null lancia NullPointerException (unchecked)
	//				se text.length() = 0 lancia IllegalTextException (checked)
	//				se text.length() > Post.MAX_LENGTH lancia IllegalTextException (checked)
	// MODIFIES:	this
	// EFFECTS:		inizializza this: this.id = pre(Post.count), post(Post.count) = pre(Post.count)+1,
	//				this.autore = autore, this.text = text, this.likes = [],
	//				this.data_pub e this.data_mod prendono il timestamp della data e ora attuali (new Date().getTime())
	
	
	// COSTRUTTORE che produce la DEEP-COPY del post p
	protected Post(Post p) throws NullPointerException {
		if(p == null) throw new NullPointerException();
		
		this.id = p.getId();
		this.autore = p.getAutore();
		this.text = p.getText();
		this.data_pub = p.getDataPub();
		this.data_mod = p.getDataMod();
		this.likes = new ArrayList<String>();
		
		for(String s : p.getLikes())
			this.likes.add(s);
	}
	// REQUIRES:	p ≠ null
	// THROWS:		se p = null lancia NullPointerException (unchecked)
	// MODIFIES:	this
	// EFFECTS:		inizializza this con la DEEP-COPY del post p preso come argomento
	//				Post.count non viene incrementato per la duplicazione di un post esistente
	
	
	// restituisce ed incrementa il totale di post creati
	private static long getNextCount() {
		return Post.count++;
	}
	// MODIFIES:	Post.count
	// EFFECTS:		post(Post.count)=pre(Post.count)+1, restituisce pre(Post.count)
	
	
	/** [METODI MODIFICATORI] **************************************************************************************/
	
	// sostituisce il testo del post con un nuovo testo, aggiornando la data di modifica
	public void modificaText(String new_text) throws NullPointerException, IllegalTextException {
		if(new_text == null) throw new NullPointerException();
		if(new_text.length() == 0) throw new IllegalTextException();
		if(new_text.length() > Post.MAX_LENGTH) throw new IllegalTextException();
		
		this.text = new_text;
		this.data_mod = new Date().getTime();
	}
	// REQUIRES:	new_text ≠ null, 0 < new_text.length() ≤ Post.MAX_LENGTH
	// THROWS:		se new_text = null lancia NullPointerException (unchecked)
	//				se new_text.length() = 0 lancia IllegalTextException (checked)
	//				se new_text.length() > Post.MAX_LENGTH lancia IllegalTextException (checked)
	// MODIFIES:	this.text, this.data_mod
	// EFFECTS:		post(this.text) = new_text e aggiorna this.data_mod al timestamp attuale (new Date().getTime())
	
	
	// persona mette like al post
	public void addLike(String persona) throws NullPointerException, AuthorLikeException, DuplicateLikeException {
		if(persona == null) throw new NullPointerException();
		if(this.autore.equals(persona)) throw new AuthorLikeException();
		if(this.likes.contains(persona)) throw new DuplicateLikeException();
		
		this.likes.add(persona);
	}
	// REQUIRES:	persona ≠ null, !this.autore.equals(persona), !this.likes.contains(persona)
	// THROWS:		se persona = null lancia NullPointerException (unchecked)
	//				se this.autore.equals(persona) lancia AuthorLikeException (checked)
	//				se this.likes.contains(persona) lancia DuplicateLikeException (checked)
	// MODIFIES:	this.likes
	// EFFECTS:		inserisce persona nella lista dei likes:  {this.likes.get(i) | 0 ≤ i < this.likes.size()} U {persona}
	
	
	// persona toglie il proprio like al post
	public void removeLike(String persona) throws NullPointerException, AuthorLikeException, AbsentLikeException {
		if(persona == null) throw new NullPointerException();
		if(this.autore.equals(persona)) throw new AuthorLikeException();
		if(!this.likes.contains(persona)) throw new AbsentLikeException();
		
		this.likes.remove(persona);
	}
	// REQUIRES:	persona ≠ null, !this.autore.equals(persona), this.likes.contains(persona)
	// THROWS:		se persona = null lancia NullPointerException (unchecked)
	//				se this.autore.equals(persona) lancia AuthorLikeException (checked)
	//				se !this.likes.contains(persona) lancia AbsentLikeException (checked)
	// MODIFIES:	this.likes
	// EFFECTS:		rimuove persona dalla lista dei likes:  {this.likes.get(i) | con 0 ≤ i < this.likes.size()} \ {persona}
	
	
	/** [METODI OSSERVATORI] ***************************************************************************************/
	
	// restituisce l'id del post
	public long getId() {
		return this.id;
	}
	// EFFECTS:		restituisce this.id
	
	// restituisce l'autore del post
	public String getAutore() {
		return this.autore;
	}
	// EFFECTS:		restituisce this.autore
	
	// restituisce il testo del post
	public String getText() {
		return this.text;
	}
	// EFFECTS:		restituisce this.text
	
	// restituisce il timestamp della data di pubblicazione del post
	public long getDataPub() {
		return this.data_pub;
	}
	// EFFECTS:		restituisce this.data_pub
	
	// restituisce il timestamp della data di ultima modifica del post
	public long getDataMod() {
		return this.data_mod;
	}
	// EFFECTS:		restituisce this.data_mod
	
	// restituisce l'insieme contenente le persone che hanno messo like al post
	public Set<String> getLikes(){
		HashSet<String> likes_set = new HashSet<String>();
		likes_set.addAll(this.likes);
		return likes_set;
	}
	// EFFECTS:		restituisce l'insieme {this.likes.get(i) | 0 ≤ i < this.likes.size()}
	
	
	/** [METODI SOVRASCRITTI (OVERRIDE)] ***************************************************************************/
	
	public Object clone() {
		return new Post(this);
	}
	// EFFECTS:		restituisce un post identico a this ottenuto tramite DEEP-COPY
	
	public boolean equals(Object o) {
		if((o == null) || !(o instanceof Post)) return false;
		Post p = (Post) o;
		return p.getId() == this.id;
	}
	// EFFECTS:		restituisce true se this e p sono lo stesso post (ovvero se hanno lo stesso id), altrimenti false
	
	public int hashCode() {
		return 0;
	}
	// EFFECTS:		restituisce sempre 0 ==> l'uguaglianza tra post dipende unicamente da equals()
	
	public String toString() {
		String s = "";
		s += String.format("[ID]:\t\t%d\n[AUTORE]:\t%s\n", this.id, this.autore);
		s += String.format("[PUB]:\t\t%s\n", new Date(this.data_pub).toString());
		s += String.format("[MOD]:\t\t%s\n", new Date(this.data_mod).toString());
		s += String.format("[LIKES]:\t");
		for(String persona : this.likes)
			s += persona + " ";
		s += '\n';
		s += String.format("[TESTO]:\t%s", this.text);
		return s;
	}
	// EFFECTS:		restituisce una stringa che rappresenta univocamente this
}

