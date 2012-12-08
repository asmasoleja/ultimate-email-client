try 
{
    //Set the content type, e.g text/html
    String contentType = message.getContentType();
    bodyTextPane.setContentType(contentType);
    System.out.println("Content type: " + contentType);
    //if (contentType.startsWith("text/html") || contentType.startsWith("test/plain"))
    //{
        System.out.println("In if!");
        InputStream inputStream = message.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String currentLine = reader.readLine();
        String message = "";
        while (currentLine != null)
        {
            System.out.println("Current line: " + currentLine);
            message += currentLine;
            currentLine = reader.readLine();
        } //while
        bodyTextPane.setText(message);
    //} //if
} //try
catch (MessagingException ex)
{
        JOptionPane.showConfirmDialog(rootPane, "Could not get body field!", "Error!", JOptionPane.OK_OPTION);
        ex.printStackTrace();
}
catch (IOException ex)
{
        JOptionPane.showConfirmDialog(rootPane, "IO error in parsing message!", "Error!", JOptionPane.OK_OPTION);
        ex.printStackTrace();
}