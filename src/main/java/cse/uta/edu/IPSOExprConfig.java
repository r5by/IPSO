package cse.uta.edu;

public class IPSOExprConfig {
	public static IPSOExprConfig ipsoExprConfig = null;
	
	private int NP;
	private int MP;
	
	private IPSOExprConfig(int NP, int MP) {
		this.NP = NP;
		this.MP = MP;
	}
	
	public static IPSOExprConfig getInstance(){
		if(ipsoExprConfig == null)
			ipsoExprConfig = new IPSOExprConfig(0, 0);
		
		return ipsoExprConfig;
	}

	//GETTERS & SETTERS
	public int NP() {
		return NP;
	}

	public void setNP(int nP) {
		NP = nP;
	}

	public int MP() {
		return MP;
	}

	public void setMP(int mP) {
		MP = mP;
	}
	
	
}
