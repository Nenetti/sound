package sound_effect;

public enum Effect {

	Active("SE.Active"), Cancel("SE.Cancel"), Question("SE.Question"), Error("SE.Error");

	public String property;

	private Effect(String property) {
		this.property = property;
	}

}