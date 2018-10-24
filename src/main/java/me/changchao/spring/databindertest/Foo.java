package me.changchao.spring.databindertest;

import java.util.List;

import lombok.Data;

@Data
public class Foo {
	private String prop1;
	private List<Bar> bar;

	@Data
	public static class Bar {
		private String name;
	}
}
