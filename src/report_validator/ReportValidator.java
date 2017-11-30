package report_validator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

import app_config.AppPaths;
import global_utils.FileUtils;
import html_viewer.HtmlViewer;

/**
 * Validate a report and show the errors to the user
 * @author avonva
 *
 */
public abstract class ReportValidator {
	
	/**
	 * Show the found errors in the default browser
	 * @param errors
	 * @return file which contains the .html contents
	 * @throws IOException if cannot save the file
	 */
	public File show(Collection<ReportError> errors) throws IOException {
		File html = save(errors);
		HtmlViewer viewer = new HtmlViewer();
		viewer.open(html);
		return html;
	}
	
	/**
	 * Save a list of errors into a temporary file
	 * @param errors
	 * @return
	 * @throws IOException 
	 */
	private File save(Collection<ReportError> errors) 
			throws IOException {
		
		File html = FileUtils.generateTempFile(".html");
		
		try(PrintWriter writer = new PrintWriter(html, "UTF-8");) {

			writer.append("<!DOCTYPE html><html>");
			
			printHeader(writer);
					
			writer.append("<body>");

			printTable(writer, errors);
			
			writer.append("</body></html>");
			writer.close();
		}
		
		return html;
	}
	
	/**
	 * Read the html header from a file and put it into the
	 * html file
	 * @param writer
	 * @throws IOException
	 */
	private void printHeader(PrintWriter writer) throws IOException {
		
		// add the header
		File header = new File(AppPaths.REPORT_ERRORS_HEAD_FILE);
		
		// add the style to the html
		if (header.exists()) {
			List<String> lines = Files.readAllLines(header.toPath());
			
			for (String line : lines)
				writer.append(line);
		}
		else {
			System.err.println("No " + AppPaths.REPORT_ERRORS_HEAD_FILE + " file found!");
		}
	}
	
	/**
	 * Print a table with a list of errors in the html file
	 * @param writer
	 * @param errors
	 */
	private void printTable(PrintWriter writer, Collection<ReportError> errors) {
		
		writer.append("<div class='span7'>");
		writer.append("<div class='widget stacked widget-table action-table'>");
		
		// widget header (with title)
		writer.append("<div class='widget-header'>");
		writer.append("<i class='icon-th-list'></i>");
		writer.append("<h3>Report errors</h3>");
		writer.append("</div>");
		
		writer.append("<div class='widget-content'>");
		
		// table
		writer.append("<table class='table table-striped table-bordered'>");
		
		// headers
		writer.append("<thead>");
		writer.append("<tr>");
		writer.append("<th scope='col'>Error type</th>");
		writer.append("<th scope='col'>Error message</th>");
		writer.append("<th scope='col'>Involved rows</th>");
		writer.append("<th scope='col'>Erroneous values</th>");
		writer.append("<th scope='col'>Suggestions</th>");	
		writer.append("</tr>");
		writer.append("</thead>");
		
		// rows
		writer.append("<tbody>");
		
		// print all the errors
		for (ReportError error : errors) {
			
			writer.append("<tr>");
			
			writer.append("<td>");
			writer.append(error.getTypeOfError().getText());
			writer.append("</td>");
			
			writer.append("<td>");
			writer.append(error.getErrorMessage());
			writer.append("</td>");

			writer.append("<td>");
			if (error.getInvolvedRowsIdsMessage() != null 
					&& !error.getInvolvedRowsIdsMessage().isEmpty()) {
				for (String rowId : error.getInvolvedRowsIdsMessage())
					writer.append("<div>" + rowId + "</div>");
			}
			writer.append("</td>");
			
			writer.append("<td>");
			if (error.getErroneousValues() != null 
					&& !error.getErroneousValues().isEmpty()) {
				for (String value : error.getErroneousValues()) {
					
					if (value.isEmpty())
						value = " - ";
					
					writer.append("<div>" + value + "</div>");
				}
			}
			writer.append("</td>");
			
			writer.append("<td>");
			if (error.getCorrectExample() != null)
				writer.append(error.getCorrectExample());
			writer.append("</td>");

			writer.append("</tr>");
		}
		
		writer.append("</tbody>");
		writer.append("</table>");
		writer.append("</div>");
		writer.append("</div>");
		writer.append("</div>");
	}
	
	/**
	 * Validate a report and return a list of errors.
	 * @return empty list if report is correct, otherwise list of errors
	 */
	public abstract Collection<ReportError> validate();
}
