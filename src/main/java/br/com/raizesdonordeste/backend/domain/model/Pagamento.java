package br.com.raizesdonordeste.backend.domain.model;

import java.math.BigDecimal;

import br.com.raizesdonordeste.backend.domain.enums.FormaPagamento;
import br.com.raizesdonordeste.backend.domain.enums.StatusPagamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pagamentos")
public class Pagamento extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pedido_id", nullable = false, unique = true)
	private Pedido pedido;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private StatusPagamento status;

	@Enumerated(EnumType.STRING)
	@Column(name = "metodo", nullable = false, length = 40)
	private FormaPagamento metodo;

	@Column(name = "valor", nullable = false, precision = 12, scale = 2)
	private BigDecimal valor;

	@Lob
	@Column(name = "payload_envio", nullable = false, columnDefinition = "TEXT")
	private String payloadEnvio;

	@Lob
	@Column(name = "payload_retorno", nullable = false, columnDefinition = "TEXT")
	private String payloadRetorno;

	@Column(name = "codigo_transacao_mock", nullable = false, length = 100)
	private String codigoTransacaoMock;
}