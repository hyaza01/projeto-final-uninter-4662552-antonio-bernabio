package br.com.raizesdonordeste.backend.api.dto.catalogo;

import java.math.BigDecimal;

import br.com.raizesdonordeste.backend.domain.enums.CategoriaProduto;

public record ProdutoCatalogoResponse(
	Long id,
	String nome,
	String descricao,
	BigDecimal preco,
	CategoriaProduto categoria
) {
}
