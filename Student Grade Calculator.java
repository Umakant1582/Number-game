private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {              
   // TODO add your handling code here:
   double mh, ch, ph, sum, pr;
   String grd;
	// getting values of subjects entered by user
   mh = Integer.parseInt(Sub3.getText());
   ch = Integer.parseInt(Sub2.getText());
   ph = Integer.parseInt(Sub1.getText());
   sum = mh + ph + ch; 
   // addition of subjects
   pr = (sum/300) * 100;  
   // calculating percentage
   if(pr >= 90) {
      grd = "A";
   } else if(pr >= 80) {
      grd = "B";
   } else if(pr >= 70) {
      grd = "C";
   } else if(pr >= 60) {
      grd = "D";
   } else if(pr >= 50) {
      grd = "E";
   } else {
      grd = "S";
   }
   // printing results in the text field of Marks, Percentage and Grade
   Marks.setText(String.valueOf(sum)); 
   Per.setText(String.valueOf(pr));
   Grade.setText(grd);
}               
