//$Id$
package org.hibernate.test.annotations.quote;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Emmanuel Bernard
 */
@Entity
@Table(name = "`User`")
public class User implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToMany
	private Set<Role> roles = new HashSet<Role>();

	// These exist solely for HHH-8464 to ensure that the various forms of quoting are normalized internally
	// (using backticks), including the join column.  Without normalization, the mapping will throw a
	// DuplicateMappingException.
	@ManyToOne
	@JoinColumn(name = "\"house\"")
	private House house;
	@Column(name = "\"house\"", insertable = false, updatable = false )
	private Long house1;
	@Column(name = "`house`", insertable = false, updatable = false )
	private Long house2;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public House getHouse() {
		return house;
	}

	public void setHouse(House house) {
		this.house = house;
	}

	public Long getHouse1() {
		return house1;
	}

	public void setHouse1(Long house1) {
		this.house1 = house1;
	}

	public Long getHouse2() {
		return house2;
	}

	public void setHouse2(Long house2) {
		this.house2 = house2;
	}
}
