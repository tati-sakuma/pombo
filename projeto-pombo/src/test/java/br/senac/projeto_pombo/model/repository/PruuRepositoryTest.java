package br.senac.projeto_pombo.model.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.senac.projeto_pombo.model.entity.Pruu;
import br.senac.projeto_pombo.model.entity.Usuario;

@SpringBootTest
@ActiveProfiles("test")
public class PruuRepositoryTest {

	@Autowired
	private PruuRepository pruuRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;
	
	private Usuario usuarioSalvo;


	@BeforeEach
	public void setUp() {
		usuarioSalvo = usuarioRepository.saveAndFlush(this.criarUsuario());

		List<Pruu> pruus = new ArrayList<>();

		Pruu pruu1 = new Pruu();
		pruu1.setMensagem("Pruu 1");
		pruu1.setUsuario(usuarioSalvo);
		pruu1.setExcluido(false);
		pruus.add(pruu1);

		Pruu pruu2 = new Pruu();
		pruu2.setMensagem("Pruu 2");
		pruu2.setUsuario(usuarioSalvo);
		pruu2.setExcluido(true);
		pruus.add(pruu2);

		pruuRepository.saveAll(pruus);
	}

	@AfterEach
	public void cleanUp() {
		pruuRepository.deleteAll();
		usuarioRepository.deleteAll();
	}

	@Test
	public void deveSalvarEPesquisarPruuComSucesso() {

		Usuario usuarioSalvo = usuarioRepository.saveAndFlush(this.criarUsuario());

		Pruu pruu = new Pruu();
		pruu.setMensagem("Mensagem de Teste");
		pruu.setUsuario(usuarioSalvo);

		Pruu pruuSalvo = pruuRepository.saveAndFlush(pruu);

		assertNotNull(pruuSalvo);
		assertEquals("Mensagem de Teste", pruuSalvo.getMensagem());
		assertEquals(usuarioSalvo.getIdUsuario(), pruuSalvo.getUsuario().getIdUsuario());
	}

	@Test
	public void deveRetornarPruusPorIdUsuario() {
		
		List<Pruu> pruus = pruuRepository.findbyIdUsuario(usuarioSalvo.getIdUsuario());

		assertNotNull(pruus);
		assertEquals(2, pruus.size());
		assertEquals("Pruu 2", pruus.get(0).getMensagem()); // O mais recente deve vir primeiro
		assertEquals("Pruu 1", pruus.get(1).getMensagem());
	}

	@Test
	public void deveRetornarTodosOsPruusOrdenadosPorData() {


		List<Pruu> pruus = pruuRepository.findAllOrderedByDataHora();

		assertNotNull(pruus);
		assertTrue(pruus.size() >= 2);
		assertEquals("Pruu 2", pruus.get(0).getMensagem()); 
		assertEquals("Pruu 1", pruus.get(1).getMensagem());
	}

	@Test
	public void deveRetornarApenasPruusAtivos() {

		List<Pruu> pruusAtivos = pruuRepository.findAtivos();

		assertNotNull(pruusAtivos);
		assertFalse(pruusAtivos.isEmpty());
		assertEquals("Pruu 1", pruusAtivos.get(0).getMensagem());
		assertTrue(pruusAtivos.stream().noneMatch(p -> p.getExcluido()));
	}

	private Usuario criarUsuario() {
		Usuario usuario = new Usuario();
		usuario.setNome("Usuario válido");
		usuario.setCpf("05262192971");
		usuario.setEmail("tati@teste.com");

		return usuario;
	}

}
