import java.util.Map;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.List;


public class ReportSocialNetwork extends SocialNetwork {
	
/**
	OVERVIEW:
	Tipo di dato modificabile che rappresenta una rete sociale di utenti
	e associa ad ogni utente i post di cui è autore e gli utenti seguiti
	e tiene traccia dei post segnalati e delle relative segnalazioni
	
	ELEMENTO TIPICO (COPPIA DI INSIEMI):
	<	{ <user, {utenti che user segue}, {post con autore user}> , ...},
		{ <post_segnalato, {<user_segnalatore, tipo_segnalazione>, ...} , ...}	>
	
	
	AF =	<	{ <user, this.followers.get(user), this.post.get(user)> , ... },
				{ <p, <this.reports.get(p).getUsername(), this.reports.get(p).getType()>> , ... }	>
		  
	
	RI =  RI(SocialNetwork) && RI(Report) &&
		  
		  &&  (∀ p ∈ this.reports.keySet()							// per ogni post p in this.reports.keySet()
			  ==> (∃ u ∈ this.post.keySet() :						// esiste un utente u in this.post.keySet()
					  (∃ p' ∈ this.post.get(u) : p = p'))			// che ha associato proprio il post p
		  &&	  this.reports.get(p) ≠ null						// p ha associato un Set<Report> non nullo
		  &&	  (∃ r ∈ this.reports.get(p))						// p ha associato almeno una segnalazione r
		  &&	  (∀ r ∈ this.reports.get(p)							// per ogni segnalazione r fatta al post p
		  		  ==> r ≠ null										// r non è nulla
		  &&		  r.getUsername() ∈ this.followers.keySet()		// utente che ha fatto r è nella rete sociale
		  &&		  r.getUsername() ≠ p.getAutore()				// r non è stata fatta dall'autore di p
		  &&		  (∀ r' ∈ this.reports.get(p) \ {r}				// per ogni altra segnalazione r' fatta al post p
		  			  ==> r'.getUsername() != r.getUsername() )))	// r' non è stata fatta dallo stesso utente di r
**/
	
	// insieme di coppie <post, {segnalazioni del post}>
	private final Map<Post, Set<Report>> reports;
	
	
	// COSTRUTTORE: istanzia una rete sociale vuota (senza segnalazioni)
	public ReportSocialNetwork() {
		super();
		this.reports = new Hashtable<Post, Set<Report>>();
	}
	// MODIFIES:	this
	// EFEFCTS:		inizializza this.followers, this.post e this.reports all'insieme vuoto (nessun utente nella rete sociale)
	
	
	// COSTRUTTORE: istanzia una rete sociale derivata da una lista di post priva di segnalazioni
	public ReportSocialNetwork(List<Post> ps) throws NullPointerException, IllegalUsernameException, DuplicatePostException {
		super(ps);
		this.reports = new Hashtable<Post, Set<Report>>();
	}
	// REQUIRES:	ps ≠ null, null ∉ ps, (∀ i,j . 0 ≤ i < j < ps.size() ==> ps.get(i) ≠ ps.get(j)),
	//				(∀ p ∈ ps ==> checkValidUsername(p.getAuthor())),
	//				(∀ p ∈ ps (∀ f ∈ p.getLikes() ==> checkValidUsername(f)))
	// THROWS:		se ps = null || null ∈ ps lancia NullPointerException (unchecked)
	//				se (∃ i,j : 0 ≤ i < j < ps.size() && ps.get(i) = ps.get(j)) lancia DuplicatePostException (checked)
	//				se (∃ p ∈ ps : !checkValidUsername(p.getAuthor())) lancia IllegalUsernameException (checked)
	//				se (∃ p ∈ ps : (∃ f ∈ p.getLikes() : !checkValidUsername(f))) lancia IllegalUsernameException (checked)
	// MODIFIES:	this
	// EFFECTS:		inizializza this.follower all'insieme di coppie <user, {follower di user}> derivate dalla lista di post
	//				inizializza this.post all'insieme di coppie <user, {post con autore user}> derivate dalla lista di post
	//				inizializza this.reports all'insieme vuoto
	
	
	
	// [MODIFICATORE] utente segnala il post della rete sociale con id specificato
	public void addReport(String username, long id, ReportType type) throws NullPointerException, IllegalArgumentException,
	AbsentPostException, IllegalUsernameException, AbsentUsernameException, DuplicateReportException, AuthorReportException {
		
		Report report = new Report(username, type); // propaga eventuali eccezioni
		if(!checkValidUsername(username)) throw new IllegalUsernameException();
		if(!this.followers.containsKey(username)) throw new AbsentUsernameException();
		Post p = this.getOriginalPost(id); // propaga eventuali eccezioni
		
		if(p.getAutore().equals(username)) throw new AuthorReportException();
		
		// se post ha almeno una segnalazione
		if(this.reports.containsKey(p)) {
			for(Report r : this.reports.get(p)) {
				// se post ha già una segnalazione dell'utente
				if(r.getUsername().equals(username)) throw new DuplicateReportException();
			}
			// aggiungo la segnalazione del post da parte dell'utente
			this.reports.get(p).add(report);
		}
		else {
			HashSet<Report> set = new HashSet<Report>();
			set.add(report);
			// aggiunge la prima segnalazione al post
			this.reports.put(p, set);
		}
	}
	// REQUIRES:	username ≠ null, type ≠ null, id >= 0, checkValidUsername(username), username ∈ this.post.keySet(),
	//				(∃ u ∈ this.post.keySet() : (∃ p ∈ this.post.get(u) : p.getId() = id && p.getAutore() ≠ username &&
	//					(p ∈ this.reports.keySet()) ==> (∀ r ∈ this.reports.get(p) ==> r.getUsername() ≠ username) ) )
	// THROWS:		se username = null || type = null lancia NullPointerException (unchecked)
	//				se id < 0 lancia IllegalArgumentException (unchecked)
	//				se !checkValidUsername(username) lancia IllegalUsernameException (checked)
	//				se username ∉ this.followers.keySet() lancia AbsentUsernameException (checked)
	//				se (∀ u ∈ this.post.keySet() (∀ p ∈ this.post.get(u) ==> p.getId() ≠ id) ) lancia AbsentPostException (checked)
	//				se p.getId() = id && p.getAutore() = username lancia AuthorReportException (checked)
	//				se (p ∈ this.reports.keySet()) && (∃ r ∈ this.reports.get(p) : r.getUsername() = username)
	//					allora lancia DuplicateReportException (checked)
	// MODIFIES:	this
	// EFFECTS:		aggiunge la segnalazione di username al post con id specificato:
	//				se p ∉ this.reports.keySet() ==> post(this.reports) = pre(this.reports) U {<p, {<username, type>}>}
	//				se p ∈ this.reports.keySet() ==> post(this.reports.get(p)) = pre(this.reports.get(p)) U {<username, type>}
	
	
	// restituisce un post della rete sociale dato l'id (post restituito senza DEEP-COPY)
	private Post getOriginalPost(long id) throws IllegalArgumentException, AbsentPostException {
		if(id < 0) throw new IllegalArgumentException();
		
		for(String user : this.post.keySet()) {		// per ogni user
			for(Post p : this.post.get(user)) {		// per ogni post di user
				if(p.getId() == id)					// se il post ha id cercato
					return p;						// restituisci post senza DEEP-COPY
			}
		}
		throw new AbsentPostException();
	}
	// REQUIRES:	id >= 0, (∃ u ∈ this.post.keySet() : (∃ p ∈ this.post.get(u) : p.getId() = id) )
	// THROWS:		se id < 0 lancia IllegalArgumentException (unchecked)
	//				se (∀ u ∈ this.post.keySet() (∀ p ∈ this.post.get(u) ==> p.getId() ≠ id) ) lancia AbsentPostException (checked)
	// EFFECTS:		restituisce il post p SENZA DEEP-COPY
		
	
	public void stampaSegnalazioni() {
		System.out.printf("POST-SEGNALAZIONI:\n");
		for(Post p : this.reports.keySet()) {
			System.out.printf("[%d] --> ", p.getId());
			for(Report r : this.reports.get(p))
				System.out.printf("[%s:%s] ", r.getUsername(), r.getType());
			System.out.println();
		}
		System.out.println("================================================");
	}
	// EFFECTS:		visualizza su stdout le segnalazioni nella rete sociale
	
	
}
