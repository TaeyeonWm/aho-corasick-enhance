package io.github.jayzhang.ac;


public class Emit {

	public int start;
    public int end;
    public String pattern;
    public Object data;

    public Emit(final int start, final int end, final String keyword, Object data) {
    	 this.start = start;
         this.end = end;
         this.data = data;
         this.pattern = keyword;
    }
    
    public Emit(final int start, final int end, final String keyword) {
        this(start, end, keyword, null);
    }

    public String getPattern() {
        return this.pattern;
    }

    public Object getData() {
		return data;
	}
 
    public String toString() {
        String t = pattern + "[" + super.toString() + "]";
        return t;
	}

}
