package org.ac;


public class Emit extends Interval implements Intervalable {

    public final String keyword;
    public Object data;

    public Emit(final int start, final int end, final String keyword, Object data) {
        super(start, end);
        this.data = data;
        this.keyword = keyword;
    }
    
    public Emit(final int start, final int end, final String keyword) {
        this(start, end, keyword, null);
    }

    public String getKeyword() {
        return this.keyword;
    }

    public Object getData() {
		return data;
	}

	@Override
    public String toString() {
        String t = keyword + "[" + super.toString() + "]";
//        if(data != null)
//        	t += "(" + data + ")";
        return t;
	}

}
