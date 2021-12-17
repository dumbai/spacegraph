package net.beadsproject.beads.file;

/**
 * Exception for use when a the file format to be written is not supported by the file type (too many channels, unsupported bit depth, for example) or
 * when the file format being read does not conform to the requirements of the file type.
 *
 * @author aengus
 */
public class FileFormatException extends Exception {
    private final String problem;

    FileFormatException(String problem) {
        super(problem);
        this.problem = problem;
    }

    public String getError() {
        return this.problem;
    }
}