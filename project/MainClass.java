import java.util.*;


public class MainClass {
		
	public static void main(String[] args) {
		
		// IMPORTANTE: eseguire i tre metodi successivi uno alla volta commentando gli altri
		
		/** 
		 * esegue i metodi della classe SocialNetwork (senza lanciare eccezioni)
		 */
		controllaSocialNetwork();
		
		/** 
		 * esegue i metodi della classe ReportSocialNetwork (senza lanciare eccezioni)
		 */
		//controllaReportSocialNetwork();
		
		/** 
		 * genera tutti i possibili casi di errore dei metodi richiesti dal progetto per le classi Post e SocialNetwork
		 * si consiglia di commentare i metodi richiamati da controllaErrori() per controllarli uno alla volta
		 */
		//controllaErrori();
		
	}
	
	public static void controllaSocialNetwork() {
		try {
			System.out.println("[POST DA CUI DERIVARE IL SOCIALNETWORK]:\n");
			// generazione lista di post validi
			List<Post> lista = generaListaPost();
			for(Post p : lista)
				System.out.println(p.toString()+"\n");
			
			System.out.println("================================================");
			System.out.println("[SOCIALNETWORK DERIVATO]:");
			// creazione rete sociale derivata da lista di post
			SocialNetwork microBlog = new SocialNetwork(lista);
			microBlog.stampa();
			
			// genera insieme di relazioni utente-seguiti derivate dalla lista di post
			// sono le stesse relazione di microBlog poiché derivate dalla stessa lista
			Map<String, Set<String>> utente_to_seguiti = SocialNetwork.guessFollowers(lista);
			
			// individua gli influencer nel precedente insieme di relazioni
			System.out.print("[INFLUENCER]: ");
			for(String f : SocialNetwork.influencers(utente_to_seguiti))
				System.out.print(f + " ");
			System.out.println("\n================================================");
			
			// individua gli utenti menzionati nei post della rete sociale
			System.out.print("[MENZIONATI]: ");
			for(String s : microBlog.getMentionedUsers())
				System.out.print(s + " ");
			System.out.println("\n================================================");
			
			// individua i post della rete sociale il cui autore è "b"
			System.out.println("[POST SCRITTI DA \"b\"]:");
			for(Post p : microBlog.writtenBy("b"))
				System.out.println("\n" + p.toString());
			System.out.println("================================================");
			
			// individua i post della rete sociale che contengono almeno una delle parola nella lista
			System.out.printf("[POST CON UNA PAROLA TRA]:\n%s\n", generaListaParole());
			for(Post p : microBlog.containing(generaListaParole())) {
				System.out.println("\n" + p.toString());
			}
			System.out.println("================================================");
			
			// aggiuge l'utente "z" alla rete sociale
			System.out.println("[CREATO UTENTE \"z\"]");
			microBlog.createUser("z");
			microBlog.stampa();
			
			// utente "g" crea un nuovo post nella rete sociale
			System.out.println("[\"g\" CREA UN POST]");
			microBlog.createPost("g", "First Post!");
			microBlog.stampa();
			
			// "z" mette like al nuovo post di "g"
			System.out.println("[\"z\" METTE LIKE AL NUOVO POST DI \"g\"]");
			microBlog.addLike("z", 5);
			microBlog.stampa();
			
			// "f" toglie like al post 3 (autore "d")
			System.out.println("[\"f\" TOGLIE LIKE AL POST ID=3 DI \"d\"]");
			microBlog.removeLike("f", 3);
			microBlog.stampa();
			
			// elimina post con id=3 (autore "d")
			System.out.println("[ELIMINA POST ID=3 DI \"d\"]");
			microBlog.deletePost(3);
			microBlog.stampa();
			
			// elimina l'utente b
			System.out.println("[ELIMINA UTENTE \"b\"]");
			microBlog.deleteUser("b");
			microBlog.stampa();
		}
		catch(Exception e) {e.printStackTrace();}	// nessuna eccezione sollevata
	}
	
	public static void controllaReportSocialNetwork() {
		try {
			System.out.println("================================================");
			System.out.println("[REPORTSOCIALNETWORK DERIVATO]:");
			ReportSocialNetwork social = new ReportSocialNetwork(generaListaPost());
			social.stampa();
			social.stampaSegnalazioni();
			
			social.addReport("c", 3, ReportType.TERRORISMO);
			social.stampaSegnalazioni();
			
			social.addReport("a", 3, ReportType.VIOLENZA);
			social.stampaSegnalazioni();
			
			social.addReport("b", 1, ReportType.SPAM_O_INGANNEVOLE);
			social.stampaSegnalazioni();
			
		} catch(Exception e) {e.printStackTrace();}	 // eccezioni non sollevate
	}
	
	// genera lista di post validi
	public static List<Post> generaListaPost(){
		ArrayList<Post> lista = new ArrayList<Post>();
		Post p;
		try {
			p = new Post("b", "Hello World.");
			p.addLike("a");
			p.addLike("c");
			p.addLike("h");
			lista.add(p);
			
			p = new Post("a", "You're welcome @alice!");
			p.addLike("b");
			p.addLike("c");
			lista.add(p);
			
			p = new Post("b", "@carlo @bob, ci vediamo domani");
			p.addLike("a");
			p.addLike("d");
			p.addLike("e");
			lista.add(p);
			
			p = new Post("d", "HI, how a are you?");
			p.addLike("f");
			p.addLike("g");
			lista.add(p);
			
			p = new Post("c", "Grazie @dario-alla prossima");
			lista.add(p);
		} catch(Exception e){}	// nessuna eccezione sollevata
		return lista;
	}
	
	// genera lista di parole per la ricerca
	public static List<String> generaListaParole(){
		ArrayList<String> lista = new ArrayList<String>();
		lista.add("hello");
		lista.add("bye");
		lista.add("WELCOME");
		lista.add("hi");
		return lista;
	}
	
	// effettua il controllo degli errori di tutti i metodi richiesti dal progetto
	public static void controllaErrori() {
		//*
		erroriCreazionePost();
		erroriModificaPost();
		erroriLikePost();
		erroriCreazioneSocialNetwork();
		erroriStaticGuessFollowers();
		erroriStaticInfluencers();
		erroriStaticGetMentionedUsers();
		erroriStaticWrittenBy();
		erroriWrittenBy();
		erroriContaining();
		//*/
	}
	
	/** [CONTROLLO ERRORI CREAZIONE POST] ********************************************************************/
	public static void erroriCreazionePost() {
		Post p;
		try {
			p = new Post(null, "testo");				// creazione post con autore null
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		try {
			p = new Post("autore", null);				// creazione post con testo null
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		try {
			p = new Post("autore", "");					// creazione post con testo vuoto
		} catch(Exception e) {e.printStackTrace();}		// cattura IllegalTextException
		try {
			String testo = "";
			for(int i=0; i<141; i++) {testo += "a";}
			p = new Post("autore", testo);				// creazione post con testo lungo più di 140 caratteri
		} catch(Exception e) {e.printStackTrace();}		// cattura IllegalTextException
		for(int i=0; i<80; i++) {System.out.print("=");} System.out.println();
	}
	
	/** [CONTROLLO ERRORI MODIFICA POST] *********************************************************************/
	public static void erroriModificaPost() {
		Post p;
		try {
			p = new Post("autore", "testo");			// creazione post valido
			p.modificaText(null);						// nuovo testo null
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		try {
			p = new Post("autore", "testo");			// creazione post valido
			p.modificaText("");							// nuovo testo vuoto
		} catch(Exception e) {e.printStackTrace();}		// cattura IllegalTextException
		try {
			p = new Post("autore", "testo");			// creazione post valido
			String testo = "";
			for(int i=0; i<141; i++) {testo += "a";}
			p.modificaText(testo);						// nuovo testo lungo più di 140 caratteri
		} catch(Exception e) {e.printStackTrace();}		// cattura IllegalTextException
		for(int i=0; i<80; i++) {System.out.print("=");} System.out.println();
	}
	
	/** [CONTROLLO ERRORI LIKE AL POST] **********************************************************************/
	public static void erroriLikePost() {
		Post p;
		try {
			p = new Post("autore", "testo");			// creazione post valido
			p.addLike(null);							// null mette like al post
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		try {
			p = new Post("autore", "testo");			// creazione post valido
			p.addLike("autore");						// autore mette like al suo post
		} catch(Exception e) {e.printStackTrace();}		// cattura AuthorLikeException
		try {
			p = new Post("autore", "testo");			// creazione post valido
			p.addLike("alice");							// alice mette like al post
			p.addLike("alice");							// alice mette un secondo like al post
		} catch(Exception e) {e.printStackTrace();}		// cattura DuplicateLikeException
		try {
			p = new Post("autore", "testo");			// creazione post valido
			p.removeLike(null);							// null toglie like al post
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		try {
			p = new Post("autore", "testo");			// creazione post valido
			p.addLike("autore");						// autore toglie like al suo post
		} catch(Exception e) {e.printStackTrace();}		// cattura AuthorLikeException
		try {
			p = new Post("autore", "testo");			// creazione post valido
			p.removeLike("alice");						// alice toglie like al post senza averlo messo
		} catch(Exception e) {e.printStackTrace();}		// cattura AbsentLikeException
		for(int i=0; i<80; i++) {System.out.print("=");} System.out.println();
	}
	
	/** [CONTROLLO ERRORI CREAZIONE SOCIALNETWORK] ***********************************************************/
	public static void erroriCreazioneSocialNetwork() {
		SocialNetwork s;
		try {
			s = new SocialNetwork(null);				// crea rete sociale derivata da null
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.add(null);							// mette null in lista
			s = new SocialNetwork(lista);				// crea rete sociale derivata dalla lista di post
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.add(lista.get(1));					// mette in lista un post duplicato
			s = new SocialNetwork(lista);				// crea rete sociale derivata dalla lista di post
		} catch(Exception e) {e.printStackTrace();}		// cattura DuplicatePostException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.add(new Post(".", "testo"));			// mette in lista un post con autore non valido
			s = new SocialNetwork(lista);				// crea rete sociale derivata dalla lista di post
		} catch(Exception e) {e.printStackTrace();}		// cattura IllegalUsernameException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.get(2).addLike("a!ice");				// utente non valido mette like ad un post nella lista
			s = new SocialNetwork(lista);				// crea rete sociale derivata dalla lista di post
		} catch(Exception e) {e.printStackTrace();}		// cattura IllegalUsernameException
		for(int i=0; i<80; i++) {System.out.print("=");} System.out.println();
	}
	
	/** [CONTROLLO ERRORI: SocialNetwork.guessFollowers] *****************************************************/
	public static void erroriStaticGuessFollowers() {
		try {
			SocialNetwork.guessFollowers(null);			// argomento: null
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.add(null);							// mette null in lista
			SocialNetwork.guessFollowers(lista);		// argomento: lista
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.add(new Post(".", "testo"));			// mette post con autore non valido in lista
			SocialNetwork.guessFollowers(lista);		// argomento: lista
		} catch(Exception e) {e.printStackTrace();}		// cattura IllegalUsernameException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.get(3).addLike(".");					// utente non valido mette like ad un post in lista
			SocialNetwork.guessFollowers(lista);		// argomento: lista
		} catch(Exception e) {e.printStackTrace();}		// cattura IllegalUsernameException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.add(lista.get(1));					// mette in lista un post duplicato
			SocialNetwork.guessFollowers(lista);		// argomento: lista
		} catch(Exception e) {e.printStackTrace();}		// cattura DuplicatePostException
		for(int i=0; i<80; i++) {System.out.print("=");} System.out.println();
	}
	
	/** [CONTROLLO ERRORI: SocialNetwork.influencers] ********************************************************/
	public static void erroriStaticInfluencers() {
		Map<String, Set<String>> f;
		try {
			SocialNetwork.influencers(null);						// argomento: null
		} catch(Exception e) {e.printStackTrace();}					// cattura NullPointerException
		try {
			f = SocialNetwork.guessFollowers(generaListaPost());	// genera f insieme di followers valido
			f.put(null, new HashSet<String>());						// mette in f un utente null
			SocialNetwork.influencers(f);							// argomento: f
		} catch(Exception e) {e.printStackTrace();}					// cattura NullPointerException
		try {
			f = SocialNetwork.guessFollowers(generaListaPost());	// genera f insieme di followers valido
			f.get("c").add(null);									// mette in f un utente null tra gli utenti seguiti da c
			SocialNetwork.influencers(f);							// argomento: f
		} catch(Exception e) {e.printStackTrace();}					// cattura NullPointerException
		try {
			f = SocialNetwork.guessFollowers(generaListaPost());	// genera f insieme di followers valido
			f.put("x", null);										// mette in f l'utente x con associato null
			SocialNetwork.influencers(f);							// argomento: f
		} catch(Exception e) {e.printStackTrace();}					// cattura NullPointerException
		try {
			f = SocialNetwork.guessFollowers(generaListaPost());	// genera f insieme di followers valido
			f.put(".", new HashSet<String>());						// mette in f un utente non valido
			SocialNetwork.influencers(f);							// argomento: f
		} catch(Exception e) {e.printStackTrace();}					// cattura IllegalUsernameException
		try {
			f = SocialNetwork.guessFollowers(generaListaPost());	// genera f insieme di followers valido
			f.get("c").add("x");									// mette in f un utente nuovo tra gli utenti seguiti da c
			SocialNetwork.influencers(f);							// argomento: f
		} catch(Exception e) {e.printStackTrace();}					// cattura IllegalArgumentException
	}
	
	/** [CONTROLLO ERRORI: SocialNetwork.getMentionedUsers] **************************************************/
	public static void erroriStaticGetMentionedUsers() {
		Post p;
		try {
			p = null;									// crea post null
			SocialNetwork.getMentionedUsers(p);			// argomento: null
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		
		try {
			ArrayList<Post> lista = null;				// crea lista di post null
			SocialNetwork.getMentionedUsers(lista);		// argomento: null
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.add(null);							// mette null in lista
			SocialNetwork.getMentionedUsers(lista);		// argomento: lista
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.add(lista.get(1));					// mette in lista un post duplicato
			SocialNetwork.getMentionedUsers(lista);		// argomento: lista
		} catch(Exception e) {e.printStackTrace();}		// cattura DuplicatePostException
		for(int i=0; i<80; i++) {System.out.print("=");} System.out.println();
	}
	
	/** [CONTROLLO ERRORI: SocialNetwork.writtenBy] **********************************************************/
	public static void erroriStaticWrittenBy() {
		try {
			SocialNetwork.writtenBy(null, "x");			// argomento: lista=null
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			SocialNetwork.writtenBy(lista, null);		// argomento: username=null
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.add(null);							// mette null in lista
			SocialNetwork.writtenBy(lista, "x");		// argomento: lista
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			SocialNetwork.writtenBy(lista, ".");		// argomento: utente non valido
		} catch(Exception e) {e.printStackTrace();}		// cattura IllegalUsernameException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.add(new Post(".", "testo"));			// mette post con autore non valido in lista
			SocialNetwork.writtenBy(lista, ".");		// argomento: lista
		} catch(Exception e) {e.printStackTrace();}		// cattura IllegalUsernameException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.add(new Post(".", "testo"));			// mette post con autore non valido in lista
			SocialNetwork.writtenBy(lista, "x");		// argomento: lista
		} catch(Exception e) {e.printStackTrace();}		// cattura IllegalUsernameException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.get(2).addLike(".");					// utente non valido mette like ad un post nella lista
			SocialNetwork.writtenBy(lista, "x");		// argomento: lista
		} catch(Exception e) {e.printStackTrace();}		// cattura IllegalUsernameException
		try {
			List<Post> lista = generaListaPost();		// crea lista di post validi
			lista.add(lista.get(1));					// mette in lista un post duplicato
			SocialNetwork.writtenBy(lista, "x");		// argomento: lista
		} catch(Exception e) {e.printStackTrace();}		// cattura DuplicatePostException
		for(int i=0; i<80; i++) {System.out.print("=");} System.out.println();
	}
	
	/** [CONTROLLO ERRORI: s.writtenBy] **********************************************************************/
	public static void erroriWrittenBy() {
		SocialNetwork s;
		try{s = new SocialNetwork(generaListaPost());}	// crea rete sociale valida
		catch(Exception e) {return;}					// non solleva eccezioni
		
		try {
			s.writtenBy(null);							// argomento: null
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		try {
			s.writtenBy(".");							// argomento: username non valido
		} catch(Exception e) {e.printStackTrace();}		// cattura IllegalUsernameException
		try {
			s.writtenBy("x");							// argomento: username non presente
		} catch(Exception e) {e.printStackTrace();}		// cattura AbsentUsernameException
		for(int i=0; i<80; i++) {System.out.print("=");} System.out.println();
	}
	
	/** [CONTROLLO ERRORI: s.containing] *********************************************************************/
	public static void erroriContaining() {	
		SocialNetwork s;
		try{s = new SocialNetwork(generaListaPost());}	// crea rete sociale valida
		catch(Exception e) {return;}					// non solleva eccezioni
		
		try {
			s.containing(null);							// argomento: null
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		try {
			List<String> lista = generaListaParole();	// crea lista di parole
			lista.add(null);							// mette null in lista
			s.containing(lista);						// argomento: lista
		} catch(Exception e) {e.printStackTrace();}		// cattura NullPointerException
		for(int i=0; i<80; i++) {System.out.print("=");} System.out.println();
	}
	
}
