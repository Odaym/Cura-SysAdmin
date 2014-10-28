package com.cura.about;

/*
 * Description: This class is used to create an object of and feed it two properties (the title and the subtitle)
 * (i.e. "Author", "TTCO Development Team")
 */

public class AboutClass {
 private String title;
 private String subtitle;

 public AboutClass(String title, String subtitle) {
  this.title = title;
  this.subtitle = subtitle;
 }

 public String getTitle() {
  return title;
 }

 public void setTitle(String title) {
  this.title = title;
 }

 public String getSubtitle() {
  return subtitle;
 }

 public void setSubtitle(String subtitle) {
  this.subtitle = subtitle;
 }
}
