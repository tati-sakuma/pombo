package br.senac.projeto_pombo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import br.senac.projeto_pombo.exception.PomboException;
import br.senac.projeto_pombo.model.dto.RelatorioDenunciaDto;
import br.senac.projeto_pombo.model.entity.Denuncia;
import br.senac.projeto_pombo.model.entity.DenunciaPk;
import br.senac.projeto_pombo.model.entity.Pruu;
import br.senac.projeto_pombo.model.entity.Usuario;
import br.senac.projeto_pombo.model.entity.enums.TiposDenuncia;
import br.senac.projeto_pombo.model.repository.DenunciaRepository;
import br.senac.projeto_pombo.model.repository.PruuRepository;
import br.senac.projeto_pombo.model.repository.UsuarioRepository;
import br.senac.projeto_pombo.model.seletor.DenunciaSeletor;
import jakarta.transaction.Transactional;

@Service
public class DenunciaService {

	@Autowired
	private DenunciaRepository repository;

	@Autowired
	private PruuRepository pruuRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	public List<Denuncia> buscarTodasDenuncias(Integer idUsuarioRequest) throws PomboException {
		Usuario usuario = usuarioRepository.findById(idUsuarioRequest)
				.orElseThrow(() -> new PomboException("Usuário não encontrado."));

		if (!usuario.getAdministrador()) {
			throw new PomboException("Apenas administrador pode buscar todas as denuncias.");
		}
		return repository.findAll();
	}

	public Denuncia buscarDenuncia(DenunciaPk idDenuncia) throws PomboException {
		return repository.findById(idDenuncia).orElseThrow(() -> new PomboException("Denúncia não encontrada!"));
	}

	public Denuncia criarDenuncia(String idPruu, Integer idUsuario, TiposDenuncia motivo) throws PomboException {
		Pruu pruu = pruuRepository.findById(idPruu).orElseThrow(() -> new PomboException("Pruu não encontrado!"));

		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new PomboException("Usuário não encontrado!"));

		DenunciaPk denunciaPk = new DenunciaPk();
		denunciaPk.setIdPruu(idPruu);
		denunciaPk.setIdUsuario(idUsuario);

		if (repository.existsById(denunciaPk)) {
			throw new PomboException("Você já denunciou esta postagem.");
		}

		Denuncia denuncia = new Denuncia();
		denuncia.setIdDenuncia(denunciaPk);
		denuncia.setPruu(pruu);
		denuncia.setUsuario(usuario);
		denuncia.setMotivo(motivo);
		denuncia.setAnalisada(false);

		pruu.setDenuncias(pruu.getDenuncias() + 1);
		pruuRepository.save(pruu);

		return repository.save(denuncia);
	}

	public Denuncia atualizarSituacaoDenuncia(DenunciaPk idDenuncia, Boolean novaSituacaoDeAnalise)
			throws PomboException {
		Denuncia denuncia = buscarDenuncia(idDenuncia);
		denuncia.setAnalisada(novaSituacaoDeAnalise);
		return repository.save(denuncia);
	}

	@Transactional
	public void excluirDenuncia(DenunciaPk idDenuncia) throws PomboException {
		if (!repository.existsById(idDenuncia)) {
			throw new PomboException("Denúncia não encontrada!");
		}
		Pruu pruu = pruuRepository.findById(idDenuncia.getIdPruu())
				.orElseThrow(() -> new PomboException("Pruu não encontrado"));
		
		pruu.setDenuncias(pruu.getDenuncias() - 1);
		pruuRepository.save(pruu);
		repository.deleteById(idDenuncia);
	}

	public List<Denuncia> pesquisarComFiltros(DenunciaSeletor denunciaSeletor) {
		if (denunciaSeletor.temFiltro() && denunciaSeletor.temPaginacao()) {
			int pageNumber = denunciaSeletor.getPagina();
			int pageSize = denunciaSeletor.getLimite();

			PageRequest pagina = PageRequest.of(pageNumber - 1, pageSize);
			return repository.findAll(denunciaSeletor, pagina).toList();
		}

		return repository.findAll(denunciaSeletor);
	}
	
	 public RelatorioDenunciaDto gerarRelatorioDenuncia(String idPruu) throws PomboException {
	        List<Denuncia> denuncias = repository.findByIdPruu(idPruu);

	        if (denuncias.isEmpty()) {
	            throw new PomboException("Nenhuma denúncia encontrada para essa mensagem!");
	        }

	        RelatorioDenunciaDto dto = new RelatorioDenunciaDto();
	        dto.setUuidMensagem(idPruu);
	        dto.setQuantidadeDenuncias(denuncias.size());

	        Integer pendentes = (int) denuncias.stream().filter(d -> d.getAnalisada().equals(false)).count();
	        Integer analisadas = (int) denuncias.stream().filter(d -> d.getAnalisada().equals(true)).count();

	        dto.setDenunciasPendentes(pendentes);
	        dto.setDenunciasAnalisadas(analisadas);

	        return dto;
	    }

}
