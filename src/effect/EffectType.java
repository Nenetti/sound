package effect;






public enum EffectType{
	Active("active");
	
	private String path;
	
	private EffectType(String path){
		this.path=System.getProperty("user.ros.se")+"/"+path;
	}
}