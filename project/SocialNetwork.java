import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Collections;


public class SocialNetwork {
	
/**
	OVERVIEW:
	Tipo di dato modificabile che rappresenta una rete sociale di utenti
	e associa ad ogni utente i post di cui è autore e gli utenti seguiti
	
	ELEMENTO TIPICO:
	{<user_0, {utenti seguiti da user_0}, {post con autore user_0}>, ..., <user_n, {utenti seguiti da user_n}, {post con autore user_n}>}
	con (0 ≤ i,j < n) AND (user_i ≠ null) AND (user_i = user_j SSE i = j)
	
	
	AF =  {<user_0, this.followers.get(user_0), this.post.get(user_0)>, ..., <user_n, this.followers.get(user_n), this.post.get(user_n)>}
		  - Ogni user_i è una stringa ≠ null univoca utilizzata come key per le tabelle hash this.followers e this.post
		  - this.followers è una tabella hash che implementa l'insieme di coppie:
				{ <user_0, {user_i | ∃ p ∈ this.post.get(user_i) : user_0 ∈ p.getLikes()}>,
			   	  ...,
			   	  <user_n, {user_i | ∃ p ∈ this.post.get(user_i) : user_n ∈ p.getLikes()}>
			   	}
		  - this.post è una tabella hash che implementa l'insieme di coppie:
			{ <user_0, {post_i | post_i.getAuthor() = user_0}>, ..., <user_n, {post_i | post_i.getAuthor() = user_n}> }
	
	
	RI =  (this.followers ≠ null) && (this.post ≠ null)					// attributi non nulli
			
		  &&  (∀ u ∈ this.followers.keySet()								// per ogni utente u in this.followers.keySet():
		  	  ==> u ≠ null												// u non nullo
		  &&      SocialNetwork.checkValidUsername(u)					// u valido
		  &&      this.followers.get(u) ≠ null							// u ha associato un Set<String> non nullo
				
		  &&  (∀ f ∈ this.followers.get(u)								// per ogni utente f seguito da u
				  ==> f ∈ this.followers.keySet()						// f è in this.followers.keySet()
		  &&		  f ≠ u												// f diverso da u
		  
		  &&		  (∃ p ∈ this.post.get(f) : u ∈ p.getLikes() )))
					  // esiste un post creato da f a cui p ha messo like
					  // ==> u NON può seguire un utente f se non ha messo like ad almeno un suo post
					  // ==> u PUO' non seguire un utente f anche se ha messo like ad un suo post ==> impedito con (*)
			
		  &&  (∀ u ∈ this.post.keySet()									// per ogni utente u in this.post.keySet():
			  ==> u ≠ null												// u non nullo
		  &&	  SocialNetwork.checkValidUsername(u)					// u valido
		  &&	  this.post.get(u) ≠ null								// u ha associato un Set<Post> non nullo
			
		  &&  (∀ p ∈ this.post.get(u)									// per ogni post p di u
				  ==> p ≠ null											// p non nullo
		  &&		  p.getAuthor() = u									// u è autore di p
					
		  &&		  (∀ l ∈ p.getLikes()								// per ogni utente l che ha messo like a p
					  ==> l ∈ this.post.keySet()							// l è in this.post.keySet()
		  &&			  u ∈ this.followers.get(l) )))					// l segue u (*)
		  
		  &&  (u ∈ this.followers.keySet()) ⇔ (u ∈ this.post.keySet())	// gli insiemi coincidono
**/
	
	// costante con totale massimo di caratteri consentiti per un nome utente
	protected static final int MAX_LENGTH_USERNAME = 24;
	
	// insieme di coppie <user, {utenti seguiti}>
	protected final Map<String, Set<String>> followers;
	// insieme di coppie <user, {post con autore user}>
	protected final Map<String, Set<Post>> post;
	
	
	// COSTRUTTORE: istanzia una rete sociale vuota
	public SocialNetwork() {
		this.followers = new Hashtable<String, Set<String>>();
		this.post = new Hashtable<String, Set<Post>>();
	}
	// MODIFIES:	this
	// EFEFCTS:		inizializza this.followers e this.post all'insieme vuoto (nessun utente nella rete sociale)
	
	
	// COSTRUTTORE: istanzia una rete sociale derivata da una lista di post
	public SocialNetwork(List<Post> ps) throws NullPointerException, IllegalUsernameException, DuplicatePostException {
		
		this.followers = SocialNetwork.guessFollowers(ps);
		this.post = new Hashtable<String, Set<Post>>();
		
		for(Post p : ps) {
			String autore = p.getAutore();
			Set<String> likes = p.getLikes();
			
			for(String like : likes) {
				// aggiunge i follower dell'autore non presenti nella tabella dei post
				// post(this.post) = pre(this.post) U {<like, {}>}
				if(!this.post.containsKey(like))
					this.post.put(like, new HashSet<Post>());
			}
			
			// se autore del post non è presente nella tabella dei post
			if(!this.post.containsKey(autore)) {
				// autore inserito nella tabella dei post
				// l'insieme dei suoi post è inizializzato con la DEEP-COPY dell'attuale post
				// post(this.post) = pre(this.post) U {<autore, {p}>}
				HashSet<Post> set = new HashSet<Post>();
				set.add((Post)p.clone());
				this.post.put(autore, set);
			}
			else {
				// DEEP-COPY del post dell'autore aggiunta al suo insieme dei post
				// this.post.get(autore) = this.post.get(autore) U {p}
				this.post.get(autore).add((Post)p.clone());
			}
		}
	}
	// REQUIRES:	ps ≠ null, null ∉ ps, (∀ i,j . 0 ≤ i < j < ps.size() ==> ps.get(i) ≠ ps.get(j)),
	//				(∀ p ∈ ps ==> checkValidUsername(p.getAuthor())),
	//				(∀ p ∈ ps (∀ f ∈ p.getLikes() ==> checkValidUsername(f)))
	// THROWS:		se ps = null || null ∈ ps lancia NullPointerException (unchecked)
	//				se (∃ i,j : 0 ≤ i < j < ps.size() && ps.get(i) = ps.get(j)) lancia DuplicatePostException (checked)
	//				se (∃ p ∈ ps : !checkValidUsername(p.getAuthor())) lancia IllegalUsernameException (checked)
	//				se (∃ p ∈ ps : (∃ f ∈ p.getLikes() : !checkValidUsername(f))) lancia IllegalUsernameException (checked)
	// MODIFIES:	this
	// EFFECTS:		inizializza this.follower all'insieme di coppie <user, {utenti che user segue}> derivate dalla lista di post
	//				inizializza this.post all'insieme di coppie <user, {post con autore user}> derivate dalla lista di post
	
	
	/** [METODI RICHIESTI] *****************************************************************************************************/
	
	// [1] restituisce la rete sociale derivata dalla lista di post (parametro del metodo)
	public static Map<String, Set<String>> guessFollowers(List<Post> ps)
	throws NullPointerException, IllegalUsernameException, DuplicatePostException {
		
		/** DuplicateUsernameException **/
		if(ps == null) throw new NullPointerException();
		Map<String, Set<String>> followers = new Hashtable<String, Set<String>>();
		
		for(Post p : ps) {
			if(p == null) throw new NullPointerException();
			if(Collections.frequency(ps, p) > 1) throw new DuplicatePostException();
			String autore = p.getAutore();
			if(!SocialNetwork.checkValidUsername(autore)) throw new IllegalUsernameException();
			
			// inserisce autore se non presente in followers: post(followers) = pre(followers) U {<autore, {}>}
			if(!followers.containsKey(autore))
				followers.put(autore, new HashSet<String>());
			
			for(String like : p.getLikes()) {
				if(!SocialNetwork.checkValidUsername(like)) throw new IllegalUsernameException();
				if(!followers.containsKey(like)) {
					// inserisce in followers l'utente che ha messo like all'autore del post
					// post(followers) = pre(followers) U {<like, {autore}>}
					HashSet<String> h = new HashSet<String>();
					h.add(autore);
					followers.put(like, h);
				}
				else {
					// aggiunge l'autore nell'insieme di utenti che like segue
					// post(followers.get(like)) = pre(followers.get(like)) U {autore}
					followers.get(like).add(autore);
				}
			}
		}
		return followers;
	}
	// REQUIRES:	ps ≠ null, null ∉ ps, (∀ i,j . 0 ≤ i < j < ps.size() ==> ps.get(i) ≠ ps.get(j)),
	//				(∀ p ∈ ps ==> checkValidUsername(p.getAuthor())),
	//				(∀ p ∈ ps (∀ f ∈ p.getLikes() ==> checkValidUsername(f)))
	// THROWS:		se ps = null || null ∈ ps lancia NullPointerException (unchecked)
	//				se (∃ i,j : 0 ≤ i < j < ps.size() && ps.get(i) = ps.get(j)) lancia DuplicatePostException (checked)
	//				se (∃ p ∈ ps : !checkValidUsername(p.getAuthor())) lancia IllegalUsernameException (checked)
	//				se (∃ p ∈ ps : (∃ f ∈ p.getLikes() : !checkValidUsername(f))) lancia IllegalUsernameException (checked)
	// EFFECTS:		restituisce l'insieme di coppie <user, {follower di user}> derivate dalla lista di post
	
	
	// [2] restituisce lista utenti che hanno un numero di follower maggiore del numero di utenti seguiti
	//     nella rete sociale derivata dall'argomento followers
	public static List<String> influencers(Map<String, Set<String>> followers)
	throws NullPointerException, IllegalArgumentException, IllegalUsernameException {
		
		if(followers == null) throw new NullPointerException();
		// controllo validità utenti nella rete sociale
		Set<String> utenti = followers.keySet();
		for(String u : utenti) {
			if(u == null) throw new NullPointerException();
			if(!SocialNetwork.checkValidUsername(u)) throw new IllegalUsernameException();
			// ogni utente ha associato un insieme di utenti seguiti non nullo (può essere vuoto)
			if(followers.get(u) == null) throw new NullPointerException();
			for(String f : followers.get(u)) {
				// un utente può seguire solo utenti presenti in followers
				if(!followers.containsKey(f)) throw new IllegalArgumentException();
				// un utente non può seguire se stesso
				if(f.equals(u)) throw new IllegalArgumentException();
			}
		}
		
		ArrayList<String> lista = new ArrayList<String>();
		for(String i : utenti) {							// per ogni utente i
			int tot_seguiti = followers.get(i).size();		// tot_seguiti = #{utenti seguiti da i}
			int tot_follower = 0;							// tot_follower = #{utenti che seguono i} (contatore)
			for(String j : utenti) {
				if(j.equals(i)) continue;					// evito il caso j = i
				if(followers.get(j).contains(i)) {			// se i è seguito da j
					tot_follower++;							// incremento tot_follower di i
					if(tot_follower > tot_seguiti) {		// se i segue tanti utenti quanti i suoi follower
						lista.add(i);						// i è un influencer ==> aggiunto alla lista da restituire
						break;
					}
				}
			}
		}
		return lista;
	}
	// REQUIRES:	followers ≠ null, null ∉ followers.keySet(),
	//				(∀ u ∈ followers.keySet() ==> checkValidUsername(u) && followers.get(u) ≠ null &&
	//					(∀ f ∈ followers.get(u) ==> f ∈ followers.keySet() && f ≠ u) )
	// THROWS:		se followers = null || null ∈ followers.keySet() lancia NullPointerException (unchecked)
	//				se (∃ u ∈ followers.keySet() : !checkValidUsername(u)) lancia IllegalUsernameException (checked)
	//				se (∃ u ∈ followers.keySet() : followers.get(u) = null) lancia NullPointerException (unchecked)
	//				se (∃ u ∈ followers.keySet() : (∃ f ∈ followers.get(u) : f ∉ followers.keySet())) lancia IllegalArgumentException (unchecked)
	//				se (∃ u ∈ followers.keySet() : (∃ f ∈ followers.get(u) : f = u)) lancia IllegalArgumentException (unchecked)
	// EFFECTS:		restituisce la lista di utenti ∈ followers.keySet() che hanno più follower rispetto agli utenti seguiti
	
	
	// [3][OSSERVATORE] restituisce l'insieme degli utenti menzionati nei post della rete sociale
	public Set<String> getMentionedUsers(){
		HashSet<String> menzionati = new HashSet<String>();
		
		for(String utente : this.post.keySet()) {
			for(Post p : this.post.get(utente))
				menzionati.addAll(SocialNetwork.getMentionedUsers(p));
		}
		return menzionati;
	}
	// EFFECTS:		restituisce l'unione di tutti gli insiemi di utenti menzionati nel testo di ogni post della rete sociale:
	//				UNIONE {m | m ∈ SocialNetwork.getMentionedUsers(p)} ∀ u ∈ this.post.keySet() (∀ p ∈ this.post.get(u))
	
	
	// [4] restituisce l'insieme degli utenti menzionati nei post della lista ps
	public static Set<String> getMentionedUsers(List<Post> ps) throws NullPointerException, DuplicatePostException {
		if(ps == null) throw new NullPointerException();
		
		HashSet<String> menzionati = new HashSet<String>();
		for(Post p : ps) {
			if(p == null) throw new NullPointerException();
			if(Collections.frequency(ps, p) > 1) throw new DuplicatePostException();
			menzionati.addAll(SocialNetwork.getMentionedUsers(p));
		}
		return menzionati;
	}
	// REQUIRES:	ps ≠ null, null ∉ ps, (∀ i,i . 0 ≤ i < j < ps.size() ==> ps.get(i) ≠ ps.get(j)),
	// THROWS:		se ps = null || null ∈ ps lancia NullPointerException (unchecked)
	//				se (∃ i,j . 0 ≤ i < j < ps.size() AND ps.get(i) = ps.get(j)) lancia DuplicatePostException (checked)
	// EFFECTS:		restituisce l'unione di tutti gli insiemi di utenti menzionati nel testo di ogni post della lista:
	//				UNIONE {m | m ∈ SocialNetwork.getMentionedUsers(p)} ∀ p ∈ ps
	
	
	// [4_extra] restituisce l'insieme degli utenti menzionati nel post preso come argomento
	public static Set<String> getMentionedUsers(Post p) throws NullPointerException {
		if(p == null) throw new NullPointerException();
		HashSet<String> menzionati = new HashSet<String>();
		
		// pattern regex per estrarre gli username menzionati dal testo del post
		// estrae i caratteri dopo la '@' fermandosi al primo carattere non alfanumerico o diverso da underscore '_'
		Matcher matcher = Pattern.compile("@(.*?)[^a-zA-Z0-9_]").matcher(p.getText()+"\0");
		
		// ciclo per l'estrazione di tutti gli utenti menzionati
		while (matcher.find()) {
			String s = matcher.group(1);
			// verifica se username menzionato è valido e aggiungilo al set da restituire
			if(SocialNetwork.checkValidUsername(s))
				menzionati.add(s);
		}
		return menzionati;
	}
	// REQUIRES:	p ≠ null
	// THROWS:		se p = null lancia NullPointerException (unchecked)
	// EFFECTS:		restituisce l'insieme degli utenti menzionati all'interno del testo del post p
	//				Un utente u è menzionato in p SSE compare in p.getText() nella forma '@'^u
	//				con u stringa di caratteri alfanumerici U {'_'} && SocialNetwork.checkValidUsername(u)
	
	
	// [5][OSSERVATORE] restituisce la lista dei post creati dall'utente username nella rete sociale
	public List<Post> writtenBy(String username) throws NullPointerException, IllegalUsernameException, AbsentUsernameException {
		
		if(username == null) throw new NullPointerException();
		if(!SocialNetwork.checkValidUsername(username)) throw new IllegalUsernameException();
		
		Set<Post> post = this.post.get(username);
		// controlla se username è presente nella rete sociale
		if(post == null) throw new AbsentUsernameException();
		
		ArrayList<Post> lista = new ArrayList<Post>();
		for(Post p : post)
			lista.add((Post)p.clone());
		
		return lista;
	}
	// REQUIRES:	username ≠ null, SocialNetwork.checkValidUsername(username), this.post.containsKey(username)
	// THROWS:		se username = null lancia NullPointerException (unchecked)
	//				se !checkValidUsername(username) lancia IllegalUsernameException (checked)
	//				se !this.post.containsKey(username) lancia AbsentUsernameException (checked)
	// EFFECTS:		restituisce la lista l di post tale che p ∈ l SSE p ∈ this.post.get(username)
	//				&& (∀ i,j . 0 ≤ i < j < l.size()  ==>  l.get(i) ≠ l.get(j))
	
	
	// [6] restituisce la lista dei post creati dall'utente username presenti nella lista ps
	public static List<Post> writtenBy(List<Post> ps, String username)
	throws NullPointerException, IllegalUsernameException, DuplicatePostException {
		
		if(ps == null || username == null) throw new NullPointerException();
		if(!SocialNetwork.checkValidUsername(username)) throw new IllegalUsernameException();
		
		ArrayList<Post> lista = new ArrayList<Post>();
		for(Post p : ps) {
			if(p == null) throw new NullPointerException();
			if(!SocialNetwork.checkValidUsername(p.getAutore())) throw new IllegalUsernameException();
			if(Collections.frequency(ps, p) > 1) throw new DuplicatePostException();
			for(String like : p.getLikes())
				if(!SocialNetwork.checkValidUsername(like)) throw new IllegalUsernameException();
			// post aggiunto alla lista da restituire
			if(p.getAutore().equals(username))
				lista.add((Post)p.clone());
		}
		return lista;
	}
	// REQUIRES:	ps ≠ null, username ≠ null, null ∉ ps, SocialNetwork.checkValidUsername(username),
	//				(∀ i,j . 0 ≤ i < j < ps.size() ==> ps.get(i) ≠ ps.get(j)),
	//				(∀ p ∈ ps ==> SocialNetwork.checkValidUsername(p.getAutore())),
	//				(∀ p ∈ ps (∀ like ∈ p.getLikes() ==> SocialNetwork.checkValidUsername(like)))
	// THROWS:		se ps = null || username = null || null ∈ ps lancia NullPointerException (unchecked)
	//				se !checkValidUsername(username) lancia IllegalUsernameException (checked)
	//				se (∃ i,j : 0 ≤ i < j < ps.size() && ps.get(i) = ps.get(j)) lancia DuplicatePostException (checked)
	//				se (∃ p ∈ ps : !checkValidUsername(p.getAutore())) lancia IllegalUsernameException (checked)
	//				se (∃ p ∈ ps : (∃ like ∈ p.getLikse() : !checkValidUsername(like))) lancia IllegalUsernameException (checked)
	// EFFECTS:		restituisce la lista l di post tale che p ∈ l SSE (p ∈ ps && p.getAuthor() = username)
	//				&& (∀ i,j . 0 ≤ i < j < l.size()  ==>  l.get(i) ≠ l.get(j))
	
	
	// [7][OSSERVATORE] restituisce la lista dei post che includono almeno una delle parole presenti nella lista
	public List<Post> containing(List<String> words) throws NullPointerException {
		if(words == null || words.contains(null)) throw new NullPointerException();
		
		// eliminazione parole duplicate e conversione in minuscolo
		HashSet<String> words_set = new HashSet<String>();
		for(String w : words)
			words_set.add(w.toLowerCase());
		
		// pattern per la ricerca delle parole (evita di cercare tra le sottostringhe di una parola del testo)
		Pattern pattern = Pattern.compile("\\b(" + String.join("|", words_set) + ")\\b");
		
		ArrayList<Post> lista = new ArrayList<Post>();
		for(String utente : this.post.keySet()) {
			for(Post p : this.post.get(utente)) {
				if(pattern.matcher(p.getText().toLowerCase()).find())
					lista.add((Post)p.clone());
			}
		}
		return lista;
	}
	// REQUIRES:	words ≠ null, null ∉ words
	// THROWS:		se words = null || null ∈ words lancia NullPointerException (unchecked)
	// EFFECTS:		restituisce la lista l contenente per ogni utente u ∈ this.post.keySet() tutti i post p ∈ this.post.get(u)
	//				che hanno in p.getText() almeno una delle parole nella lista words
	//				&& (∀ i,j . 0 ≤ i < j < l.size()  ==>  l.get(i) ≠ l.get(j))
	//				Una striga s contiene una parola w SSE esite una sottostringa t di s identica a w tale che
	//				t è delimitata in s da caratteri di formattazione o punteggiatura
	
	
	/** [METODI NON RICHIESTI] *************************************************************************************************/
	
	// restituisce true se la stringa rappresenta un nome utente valido
	protected static boolean checkValidUsername(String username) {
		if(username.length() == 0 || username.length() > SocialNetwork.MAX_LENGTH_USERNAME) return false;
		if(Character.isDigit(username.charAt(0))) return false;
		if(!username.matches("[a-z0-9_]+")) return false;
		return true;
	}
	// EFFECTS:		restituisce false se username è una stringa vuota o se ha più di MAX_LENGTH_USERNAME caratteri
	//				o se il primo carattere è una cifra
	//				o se contiene altri caratteri oltre a quelli alfanumerici minuscoli e all'underscore '_',
	//				altrimenti restituisce true
	
	
	// [MODIFICATORE] aggiunge un nuovo utente alla rete sociale
	public void createUser(String username) throws NullPointerException, IllegalUsernameException, DuplicateUsernameException {
		if(username == null) throw new NullPointerException();
		if(!checkValidUsername(username)) throw new IllegalUsernameException();
		if(this.followers.containsKey(username)) throw new DuplicateUsernameException();
		
		this.followers.put(username, new HashSet<String>());
		this.post.put(username, new HashSet<Post>());
	}
	// REQUIRES:	username ≠ null, checkValidUsername(username), !this.followers.containsKey(username)
	// THROWS:		se username = null lancia NullPointerException (unchecked)
	//				se !checkValidUsername(username) lancia IllegalUsernameException (checked)
	//				se this.followers.containsKey(username) lancia DuplicateUsernameException (checked)
	// MODIFIES:	this
	// EFFECTS:		aggiunge a this.followers e this.post la chiave username e l'insieme vuoto come rispettivo valore associato
	//				post(this.followers) = pre(this.followers) U {<username,{}>}
	//				post(this.post) = pre(this.post) U {<username,{}>}
	
	
	// [MODIFICATORE] elimina utente dalla rete sociale restituendo i post di cui è autore
	public Set<Post> deleteUser(String username) throws NullPointerException, IllegalUsernameException, AbsentUsernameException {
		
		if(username == null) throw new NullPointerException();
		if(!checkValidUsername(username)) throw new IllegalUsernameException();
		if(!this.followers.containsKey(username)) throw new AbsentUsernameException();
		
		// rimozione dei like messi dall'utente
		for(String seguito : this.followers.get(username)) {
			for(Post p : this.post.get(seguito)) {
				try {p.removeLike(username);}
				catch(AbsentLikeException e) {}		// continuo
				catch(AuthorLikeException e) {}		// non viene lanciata mai
			}
		}
		
		// rimozione utente dalla tabella dei follower
		this.followers.remove(username);
		
		// rimozione username dall'insieme di utenti seguiti da tutti gli altri utenti
		for(String u : this.followers.keySet())
			this.followers.get(u).remove(username);
		
		// rimozione utente dalla tabella dei post e restituzione dell'insieme dei post di cui è autore
		return this.post.remove(username);
	}
	// REQUIRES:	username ≠ null, checkValidUsername(username), this.followers.containsKey(username)
	// THROWS:		se username = null lancia NullPointerException (unchecked)
	//				se !checkValidUsername(username) lancia IllegalUsernameException (checked)
	//				se !this.followers.containsKey(username) lancia AbsentUsernameException (checked)
	// MODIFIES:	this
	// EFFECTS:		(∀ s ∈ pre(this.followers.get(username)) (∀ p ∈ this.post.get(s)
	//					==> post(p.getLikes()) = pre(p.getLikes()) \ {username} ) )
	//				post(this.followers.keySet()) = pre(this.followers.keySet()) \ {username}
	//				(∀ u ∈ post(this.followers.keySet())
	//					==> post(this.followers.get(u)) = pre(this.followers.get(u)) \ {username} )
	//				post(this.post.keySet()) = pre(this.post.keySet()) \ {username}
	//				restituisce pre(this.post.get(username))
	
	
	// [MODIFICATORE] crea un nuovo post nella rete sociale
	public void createPost(String autore, String text)
	throws NullPointerException, IllegalTextException, IllegalUsernameException, AbsentUsernameException {
		
		// tenta di creare il nuovo post
		// eventuali eccezioni del costruttore Post vengono propagate (throws)
		Post p = new Post(autore, text);
		
		if(!SocialNetwork.checkValidUsername(autore)) throw new IllegalUsernameException();
		if(!this.followers.containsKey(autore)) throw new AbsentUsernameException();
		
		// post aggiunto all'insieme dei post dell'autore
		this.post.get(autore).add(p);
	}
	// REQUIRES:	Post(autore, text) valido, checkValidUsername(autore), this.followers.containsKey(autore)
	// THROWS:		se Post(autore, text) non valido propaga l'eccezione sollevata dal costruttore del tipo Post:
	// 				  - se autore = null || text = null lancia NullPointerException (unchecked)
	//				  - se text.length() = 0 lancia IllegalTextException (checked)
	//				  - se text.length() > Post.MAX_LENGTH lancia IllegalTextException (checked)
	//				se !checkValidUsername(username) lancia IllegalUsernameException (checked)
	//				se !this.followers.containsKey(username) lancia AbsentUsernameException (checked)
	// MODIFIES:	this (this.post)
	// EFFECTS:		crea un nuovo post p e lo aggiunge all'insieme dei post dell'autore nella rete sociale
	//				post(this.post.get(autore)) = pre(this.post.get(autore)) U {p}	
	
	
	// [MODIFICATORE] elimina e restituisce un post nella rete sociale dato l'id
	public Post deletePost(long id) throws IllegalArgumentException, AbsentPostException {
		
		Post found = this.getPost(id);		// eventuali eccezioni proagate
		String autore = found.getAutore();
		this.post.get(autore).remove(found);
		
		HashSet<String> all_likes = new HashSet<String>();
		for(Post p : this.post.get(autore)) {
			all_likes.addAll(p.getLikes());
		}
		for(String u : found.getLikes()) {
			if(!all_likes.contains(u))
				this.followers.get(u).remove(autore);
		}
		
		return found;
	}
	// REQUIRES:	id >= 0, (∃ u ∈ this.post.keySet() : (∃ p ∈ this.post.get(u) : p.getId() = id) )
	// THROWS:		se id < 0 lancia IllegalArgumentException (unchecked)
	//				se (∀ u ∈ this.post.keySet() (∀ p ∈ this.post.get(u) ==> p.getId() ≠ id) ) lancia AbsentPostException (checked)
	// MODIFIES:	this
	// EFFECTS:		individua il post p tale che p.getId() = id,
	//				post(this.post.get(p.getAutore())) = pre(this.post.get(p.getAutore())) \ {p},
	//				rimuove p.getAutore() dall'insieme di utenti seguiti da quelli che hanno messo like a p
	//				SSE p.getAutore() non ha più post in cui hanno messo like
	//				e restituisce p
	
	
	// [MODIFICATORE] elimina e restituisce tutti i post di un utente nella rete sociale
	public Set<Post> deleteAllPost(String username) throws NullPointerException, IllegalUsernameException, AbsentUsernameException {
		
		if(username == null) throw new NullPointerException();
		if(!checkValidUsername(username)) throw new IllegalUsernameException();
		if(!this.followers.containsKey(username)) throw new AbsentUsernameException();
		
		// estraggo l'insieme di post dell'utente per restituirlo
		Set<Post> post = this.post.get(username);
		// sovrascrivo l'insieme di post dell'utente con l'insieme vuoto
		this.post.put(username, new HashSet<Post>());
		
		// costruisco insieme di utenti che seguivano username
		HashSet<String> likes = new HashSet<String>();
		for(Post p : post)
			likes.addAll(p.getLikes());
		// rimuovo username dall'insieme di utenti seguiti da ogni follower di username
		for(String l : likes)
			this.followers.get(l).remove(username);
		
		return post;
	}
	// REQUIRES:	username ≠ null, checkValidUsername(username), this.followers.containsKey(username)
	// THROWS:		se username = null lancia NullPointerException (unchecked)
	//				se !checkValidUsername(username) lancia IllegalUsernameException (checked)
	//				se !this.followers.containsKey(username) lancia AbsentUsernameException (checked)	
	// MODIFIES:	this
	// EFFECTS:		post(this.post.get(username)) = {},
	//				rimuove username dall'insieme di utenti seguiti di coloro che hanno messo like
	//				ad almeno uno dei post in pre(this.post.get(username))
	//				e restituisce pre(this.post.get(username))
	
	
	// [OSSERVATORE] restituisce un post della rete sociale dato l'id (post restituito)
	public Post getPost(long id) throws IllegalArgumentException, AbsentPostException {
		if(id < 0) throw new IllegalArgumentException();
		
		for(String user : this.post.keySet()) {		// per ogni user
			for(Post p : this.post.get(user)) {		// per ogni post di user
				if(p.getId() == id)					// se il post ha id cercato
					return (Post)p.clone();			// restituisci la DEEP-COPY del post
			}
		}
		throw new AbsentPostException();
	}
	// REQUIRES:	id >= 0, (∃ u ∈ this.post.keySet() : (∃ p ∈ this.post.get(u) : p.getId() = id) )
	// THROWS:		se id < 0 lancia IllegalArgumentException (unchecked)
	//				se (∀ u ∈ this.post.keySet() (∀ p ∈ this.post.get(u) ==> p.getId() ≠ id) ) lancia AbsentPostException (checked)
	// EFFECTS:		restituisce la DEEP-COPY del post p
	
	
	// [MODIFICATORE] utente mette like al post nella rete sociale con l'id specificato
	public void addLike(String username, long id) throws NullPointerException, IllegalArgumentException,
	IllegalUsernameException, AbsentUsernameException, AbsentPostException, AuthorLikeException, DuplicateLikeException {
		
		if(username == null) throw new NullPointerException();
		if(id < 0) throw new IllegalArgumentException();
		if(!checkValidUsername(username)) throw new IllegalUsernameException();
		if(!this.followers.containsKey(username)) throw new AbsentUsernameException();
		
		for(String u : this.post.keySet()) {
			for(Post p : this.post.get(u)) {
				if(p.getId() == id) {
					p.addLike(username);	// eventuali eccezioni propagate
					// username segue l'utente a cui ha messo like
					this.followers.get(username).add(p.getAutore());
					return;
				}
			}
		}
		throw new AbsentPostException();
	}
	// REQUIRES:	username != null, id >= 0, SocialNetwork.checkValidUsername(username), this.post.containsKey(username),
	//				(∃ u ∈ this.post.keySet() : (∃ p ∈ this.post.get(u) :
	//					p.getId() = id && p.getAutore() ≠ username && username ∉ p.getLikes() ) )
	// THROWS:		se username = null lancia NullPointerException (unchecked)
	//				se id < 0 lancia IllegalArgumentException (unchecked)
	//				se !checkValidUsername(username) lancia IllegalUsernameException (checked)
	//				se !this.post.containsKey(username) lancia AbsentUsernameException (checked)
	//				se (∀ u ∈ this.post.keySet() (∀ p ∈ this.post.get(u) ==> p.getId() ≠ id) ) lancia AbsentPostException (checked)
	//				se p.getId() = id && p.getAutore() = username lancia AuthorLikeException (checked)
	//				se p.getId() = id && username ∈ p.getLikes() lancia DuplicateLikeException (checked)
	// MODIFIES:	this
	// EFFECTS:		aggiunge il like di username al post con l'id specificato
	//				post(this.followers.get(username)) = pre(this.followers.get(username)) U {p.getAutore()}
	
	
	// [MODIFICATORE] utente rimuove like dal post nella rete sociale con l'id specificato
	public void removeLike(String username, long id) throws NullPointerException, IllegalArgumentException,
	IllegalUsernameException, AbsentUsernameException, AbsentPostException, AuthorLikeException, AbsentLikeException {
		
		if(username == null) throw new NullPointerException();
		if(id < 0) throw new IllegalArgumentException();
		if(!checkValidUsername(username)) throw new IllegalUsernameException();
		if(!this.followers.containsKey(username)) throw new AbsentUsernameException();
		
		for(String u : this.post.keySet()) {
			for(Post p : this.post.get(u)) {
				if(p.getId() == id) {
					p.removeLike(username);		// eventuali eccezioni propagate
					// termina se c'è un altro post di u a cui username ha messo like
					for(Post post : this.post.get(u)) {
						if(post.getLikes().contains(username))
							return;
					}
					// se non c'è più alcun post di u a cui username ha messo like
					// rimuove u dall'insieme di utenti che username segue e termina
					this.followers.get(username).remove(u);
					return;
				}
			}
		}
		throw new AbsentPostException();
	}
	// REQUIRES:	username != null, id >= 0, SocialNetwork.checkValidUsername(username), this.followers.containsKey(username),
	//				(∃ u ∈ this.post.keySet() : (∃ p ∈ this.post.get(u) :
	//					p.getId() = id && p.getAutore() ≠ username && username ∈ p.getLikes() ) )
	// THROWS:		se username = null lancia NullPointerException (unchecked)
	//				se id < 0 lancia IllegalArgumentException (unchecked)
	//				se !checkValidUsername(username) lancia IllegalUsernameException (checked)
	//				se !this.post.containsKey(username) lancia AbsentUsernameException (checked)
	//				se (∀ u ∈ this.post.keySet() (∀ p ∈ this.post.get(u) ==> p.getId() ≠ id) ) lancia AbsentPostException (checked)
	//				se p.getId() = id && p.getAutore() = username lancia AuthorLikeException (checked)
	//				se p.getId() = id && username ∉ p.getLikes() lancia AbsentLikeException (checked)
	// MODIFIES:	this
	// EFFECTS:		rimuove il like di username al post con l'id specificato
	//				post(this.followers.get(username)) = pre(this.followers.get(username)) \ {p.getAutore()}
	//				SSE ( p.getId() = id && (∀ t ∈ post(this.post.get(p.getAutore())) ==> username ∉ t.getLikes()) )
	
	
	// mostra lo stato attuale della rete sociale
	public void stampa() {
		System.out.printf("================================================\n");
		System.out.printf("UTENTE-SEGUITI:\n");
		for(String utente : this.followers.keySet()) {
			System.out.printf("[%s] --> {", utente);
			for(String follower : this.followers.get(utente))
				System.out.printf("%s ", follower);
			System.out.printf("}\n");
		}
		System.out.printf("\nUTENTE-POST:\n");
		for(String utente : this.post.keySet()) {
			System.out.printf("[%s] --> ", utente);
			for(Post p : this.post.get(utente))
				System.out.printf("|%s| ", p.getId());
			System.out.println();
		}
		System.out.printf("\nPOST-LIKES:\n");
		for(String utente : this.post.keySet()) {
			for(Post p : this.post.get(utente)) {
				System.out.printf("[%s] --> ", p.getId());
				for(String like : p.getLikes())
					System.out.printf("|%s| ", like);
				System.out.println();
			}
		}
		System.out.printf("================================================\n");
	}
	// EFFECTS:		visualizza su stdout lo stato della rete sociale
}
