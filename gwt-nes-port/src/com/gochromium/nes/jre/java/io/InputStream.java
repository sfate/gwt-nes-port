package java.io;

public abstract class InputStream {
	public abstract int read() throws IOException;

	public int read(byte[] buf, int start, int len) throws IOException {

		int end = start + len;
		for (int i = start; i < end; i++) {
			int r = read();
			if (r == -1) {
				return i == start ? -1 : i - start;
			}
			buf[i] = (byte) r;
		}
		return len;
	}

	public int read(byte[] buf) throws IOException {
		return read(buf, 0, buf.length);
	}

	public void close() throws IOException {

	}

	public synchronized void mark(int readlimit) {}
	
    public boolean markSupported() {
    	return false;
    }

    public synchronized void reset() throws IOException {
    	throw new IOException("mark/reset not supported");
        }
    
    public int available() throws IOException {
    	return 0;
        }
    
    
    // SKIP_BUFFER_SIZE is used to determine the size of skipBuffer
    private static final int SKIP_BUFFER_SIZE = 2048;
    // skipBuffer is initialized in skip(long), if needed.
    private static byte[] skipBuffer;
    
    public long skip(long n) throws IOException {

    	long remaining = n;
    	int nr;
    	if (skipBuffer == null)
    	    skipBuffer = new byte[SKIP_BUFFER_SIZE];

    	byte[] localSkipBuffer = skipBuffer;
    		
    	if (n <= 0) {
    	    return 0;
    	}

    	while (remaining > 0) {
    	    nr = read(localSkipBuffer, 0,
    		      (int) Math.min(SKIP_BUFFER_SIZE, remaining));
    	    if (nr < 0) {
    		break;
    	    }
    	    remaining -= nr;
    	}
    	
    	return n - remaining;
        }
}
