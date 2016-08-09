/**
 * The Blue Alliance API
 * Access data about the FIRST Robotics Competition
 *
 * OpenAPI spec version: 2
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.thebluealliance.api.model;

import java.util.Objects;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.annotation.Nullable;


/**
 * Award
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-08-09T14:04:30.906-04:00")
public class Award   {
  @SerializedName("award_type")
  private Integer awardType = null;

  @SerializedName("event_key")
  private String eventKey = null;

  @SerializedName("key")
  private String key = null;

  @SerializedName("lastModified")
  private Long lastModified = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("recipient_list")
  private String recipientList = null;

  @SerializedName("year")
  private Integer year = null;

  public Award awardType(Integer awardType) {
    this.awardType = awardType;
    return this;
  }

   /**
   * An integer that represents the award type as a constant.
   * @return awardType
  **/
  @ApiModelProperty(example = "null", value = "An integer that represents the award type as a constant.")
  @Nullable
  public Integer getAwardType() {
    return awardType;
  }

  public void setAwardType(Integer awardType) {
    this.awardType = awardType;
  }

  public Award eventKey(String eventKey) {
    this.eventKey = eventKey;
    return this;
  }

   /**
   * The event_key of the event the award was won at.
   * @return eventKey
  **/
  @ApiModelProperty(example = "null", value = "The event_key of the event the award was won at.")
  @Nullable
  public String getEventKey() {
    return eventKey;
  }

  public void setEventKey(String eventKey) {
    this.eventKey = eventKey;
  }

  public Award key(String key) {
    this.key = key;
    return this;
  }

   /**
   * Unique key for this Award, formatted like <event key>:<type enum>
   * @return key
  **/
  @ApiModelProperty(example = "null", value = "Unique key for this Award, formatted like <event key>:<type enum>")
  @Nullable
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Award lastModified(Long lastModified) {
    this.lastModified = lastModified;
    return this;
  }

   /**
   * Timestamp this model was last modified
   * @return lastModified
  **/
  @ApiModelProperty(example = "null", value = "Timestamp this model was last modified")
  @Nullable
  public Long getLastModified() {
    return lastModified;
  }

  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  public Award name(String name) {
    this.name = name;
    return this;
  }

   /**
   * The name of the award as provided by FIRST. May vary for the same award type.
   * @return name
  **/
  @ApiModelProperty(example = "null", value = "The name of the award as provided by FIRST. May vary for the same award type.")
  @Nullable
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Award recipientList(String recipientList) {
    this.recipientList = recipientList;
    return this;
  }

   /**
   * A list of recipients of the award at the event. Either team_number or awardee for individual awards.
   * @return recipientList
  **/
  @ApiModelProperty(example = "null", value = "A list of recipients of the award at the event. Either team_number or awardee for individual awards.")
  @Nullable
  public String getRecipientList() {
    return recipientList;
  }

  public void setRecipientList(String recipientList) {
    this.recipientList = recipientList;
  }

  public Award year(Integer year) {
    this.year = year;
    return this;
  }

   /**
   * The year this award was won.
   * @return year
  **/
  @ApiModelProperty(example = "null", value = "The year this award was won.")
  @Nullable
  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Award award = (Award) o;
    return Objects.equals(this.awardType, award.awardType) &&
        Objects.equals(this.eventKey, award.eventKey) &&
        Objects.equals(this.key, award.key) &&
        Objects.equals(this.lastModified, award.lastModified) &&
        Objects.equals(this.name, award.name) &&
        Objects.equals(this.recipientList, award.recipientList) &&
        Objects.equals(this.year, award.year);
  }

  @Override
  public int hashCode() {
    return Objects.hash(awardType, eventKey, key, lastModified, name, recipientList, year);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Award {\n");
    
    sb.append("    awardType: ").append(toIndentedString(awardType)).append("\n");
    sb.append("    eventKey: ").append(toIndentedString(eventKey)).append("\n");
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    lastModified: ").append(toIndentedString(lastModified)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    recipientList: ").append(toIndentedString(recipientList)).append("\n");
    sb.append("    year: ").append(toIndentedString(year)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

