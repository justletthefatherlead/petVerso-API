package com.namassacompany.petVersoRestFull.service;

import com.namassacompany.petVersoRestFull.dto.*;
import com.namassacompany.petVersoRestFull.exception.AcessoNegadoException;
import com.namassacompany.petVersoRestFull.exception.FotoInvalidaException;
import com.namassacompany.petVersoRestFull.exception.PetNaoEncontradoException;
import com.namassacompany.petVersoRestFull.exception.StatusDeVinculoInvalidoException;
import com.namassacompany.petVersoRestFull.model.*;
import com.namassacompany.petVersoRestFull.repository.PetRepository;
import com.namassacompany.petVersoRestFull.repository.VinculoPetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class PetService {
    private static final SecureRandom random = new SecureRandom();
    private static final String caracteres = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private final PetRepository petRepository;
    private final VinculoPetRepository vinculoPetRepository;
    private final VinculoAcessoService vinculoAcessoService;

    public PetService(PetRepository petRepository, VinculoPetRepository vinculoPetRepository, VinculoAcessoService vinculoAcessoService) {
        this.petRepository = petRepository;
        this.vinculoPetRepository = vinculoPetRepository;
        this.vinculoAcessoService = vinculoAcessoService;
    }

    @Transactional
    public PetFotoRequestDTO atualizarFotoPet(Long id, Usuario usuario, PetFotoRequestDTO dto) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new PetNaoEncontradoException("Pet não encontrado"));

        VinculoPet vinculo = vinculoAcessoService.buscarVinculoAceito(pet, usuario,()-> new PetNaoEncontradoException("Pet nao encontrado"));
        if (vinculo.getPapel() != Papel.DONO) {
            throw new AcessoNegadoException("Seu papel não permite fazer alterações");
        }

        if (dto.fotoBase64() == null || dto.fotoBase64().isBlank()) {
            throw new FotoInvalidaException("Foto não informada");
        }

        byte[] foto;
        try {
            foto = Base64.getDecoder().decode(dto.fotoBase64());
        } catch (IllegalArgumentException e) {
            throw new FotoInvalidaException("Foto em formato Base64 inválido");
        }

        if (foto.length > TAMANHO_MAXIMO_FOTO) {
            throw new FotoInvalidaException("Foto excede o tamanho máximo permitido");
        }

        pet.setFoto(foto);
        Pet salvo = petRepository.save(pet);

        return new PetFotoRequestDTO(Base64.getEncoder().encodeToString(salvo.getFoto()));
    }
    @Transactional
   public PetResponseDTO cadastrar(PetRequestDTO petdto, Usuario usuario){
       String codigoVinculo = gerarCodigoVinculo();
        byte[] foto= null;
        if (petdto.fotoBase64() != null){
           foto =(Base64.getDecoder().decode(petdto.fotoBase64()));
        }
        Pet pet = new Pet(
                null,
                petdto.nome(),
                petdto.raca(),
                petdto.especie(),
                petdto.dataDeNascimento(),
                petdto.porte(),
                petdto.peso(),
                petdto.sexo(),
                null,
                foto,
                codigoVinculo,
                new ArrayList<>()


        );
        Pet petSalvo = petRepository.save(pet);

       VinculoPet vinculoPet = new VinculoPet(
               null,
               petSalvo,
               usuario,
               Papel.DONO,
               StatusDeVinculo.ACEITO,
               LocalDateTime.now()

       );
       vinculoPetRepository.save(vinculoPet);


        return new PetResponseDTO(
                petSalvo
        );
   }

    @Transactional(readOnly = true)
   public PetPerfilResponseDTO visualizarPetPerfil(Long idPet, Usuario usuarioAutenticado){
        Pet pet = petRepository.findById(idPet).
                orElseThrow(() -> new PetNaoEncontradoException("Pet nao encontrado"));
        Papel papel = vinculoAcessoService.buscarVinculoAceito(pet, usuarioAutenticado, ()-> new PetNaoEncontradoException("Pet nao encontrado")).getPapel();

        return new PetPerfilResponseDTO(pet,papel);
   }

    public PetPerfilResponseDTO addPerfilSensiAndPersonalit(Long idPet, PetPerfilRequestDTO pdto, Usuario usuario){
        Pet pet = petRepository.findById(idPet).orElseThrow(()-> new PetNaoEncontradoException("Pet nao encontrado"));
        VinculoPet vinculo = vinculoAcessoService.buscarVinculoAceito(pet, usuario,()-> new PetNaoEncontradoException("Pet nao encontrado"));
        if (vinculo.getPapel()!= Papel.DONO){
            throw new AcessoNegadoException("Seu papel nao permite fazer alterações");
        }
        if(pdto.perfilDeSensibilidade()!= null){
            pet.setPerfilSensibilidade(pdto.perfilDeSensibilidade());
        }
        if (pdto.personalidades()!= null){
            pet.setPersonalidades(pdto.personalidades());
        }

        return new PetPerfilResponseDTO(petRepository.save(pet),vinculo.getPapel());
    }

    @Transactional
    public VinculoResponseDTO solicitarVinculo(Usuario usuario, VinculoRequestDTO dto){
        String codigo  = dto.codigoVinculo();
        Pet pet = petRepository.findByCodigoVinculo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Código de vínculo inválido"));

        Optional<VinculoPet> vinculoExistente = vinculoPetRepository.findByPetAndUsuario(pet, usuario);
            VinculoPet vinculo;
            if(vinculoExistente.isPresent()){
                vinculo = vinculoExistente.get();
                switch (vinculo.getStatus()){
                    case PENDENTE -> throw new IllegalStateException("Você já possui uma soliciçao para este pet.");
                    case ACEITO -> throw new IllegalStateException("Você já possui vinculo com este pet.");
                    case RECUSADO -> {
                        vinculo.setStatus(StatusDeVinculo.PENDENTE);
                        vinculo.setDataDeCriacao(LocalDateTime.now());
                    }
                }
            }else {
                vinculo = new VinculoPet();
                vinculo.setPet(pet);
                vinculo.setUsuario(usuario);
                vinculo.setPapel(Papel.SUPORTE);
                vinculo.setStatus(StatusDeVinculo.PENDENTE);
                vinculo.setDataDeCriacao(LocalDateTime.now());


            }
            vinculoPetRepository.save(vinculo);
            var dados = new VinculoResponseDTO.DadosVinculoResponseDTO(
                    vinculo.getId(),
                    codigo,
                    pet.getNome(),
                    vinculo.getDataDeCriacao()
            );
            return new VinculoResponseDTO(
                    true,
                    "Solicitação enviada com sucesso! Aguarde a confirmação do tutor",
                    StatusDeVinculo.PENDENTE,
                    dados
            );
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponseDTO> listarSolicitacoes(Usuario usuario){
        List<VinculoPet> vinculosDono = vinculoPetRepository.findByUsuarioAndPapel(usuario, Papel.DONO);
        List<SolicitacaoResponseDTO> resultado = new ArrayList<>();

        for (VinculoPet vinculoDono : vinculosDono) {
            if (vinculoDono.getStatus()!= StatusDeVinculo.ACEITO){
                continue;
            }
            Pet pet = vinculoDono.getPet();

            List<VinculoPet> pendentes = vinculoPetRepository.findByPetAndStatus(pet,StatusDeVinculo.PENDENTE);
            for (VinculoPet solicitacao : pendentes){
                resultado.add(new SolicitacaoResponseDTO(
                        solicitacao.getId(),
                        solicitacao.getUsuario().getNome(),
                        pet.getNome(),
                        solicitacao.getDataDeCriacao()
                ));
            }
        }
        return  resultado;
    }
    @Transactional
    public VinculoResponseDTO processarSolicitacao(Long idSolicitacao, StatusDeVinculo novoStatus, Usuario usuario){
        VinculoPet solicitacao =  vinculoPetRepository.findById(idSolicitacao).orElseThrow(()-> new PetNaoEncontradoException("solicitacao nao existe"));
        VinculoPet meuVinculo = vinculoPetRepository.findByPetAndUsuario(solicitacao.getPet(), usuario).orElseThrow(()-> new PetNaoEncontradoException("solicitacao nao existe"));
        if(meuVinculo.getPapel()!= Papel.DONO || meuVinculo.getStatus() != StatusDeVinculo.ACEITO){
            throw new AcessoNegadoException("Voce nao tem permissao para realizar esta acao");
        }
        if (novoStatus != StatusDeVinculo.ACEITO && novoStatus != StatusDeVinculo.RECUSADO){
            throw new StatusDeVinculoInvalidoException("Só pode ser aceito ou recusado");
        }
        if (solicitacao.getStatus()!= StatusDeVinculo.PENDENTE){
            throw new StatusDeVinculoInvalidoException("Esta solicitacao já foi respondida anteriormente");
        }
        solicitacao.setStatus(novoStatus);
        vinculoPetRepository.save(solicitacao);

        return new VinculoResponseDTO(
                   true,
                    "concluido",
                    novoStatus,
                new VinculoResponseDTO.DadosVinculoResponseDTO(
                        solicitacao.getId(),
                        solicitacao.getPet().getCodigoVinculo(),
                        solicitacao.getPet().getNome(),
                        solicitacao.getDataDeCriacao()
                )

        );


    }

    @Transactional(readOnly = true)
    public List<PetResumoResponseDTO> listarPets(Usuario usuario){
        List<VinculoPet> vinculos = vinculoPetRepository.findByUsuarioAndStatus(usuario, StatusDeVinculo.ACEITO);
        return vinculos.stream().map(PetResumoResponseDTO::new).toList();
    }

    public PetResponseDTO alterarDados(Long idPet, PetRequestDTO dto, Usuario usuario){
        Pet pet = petRepository.findById(idPet).orElseThrow(()-> new PetNaoEncontradoException("Pet nao encontrado"));
        vinculoAcessoService.buscarVinculoAceito(pet, usuario,()-> new PetNaoEncontradoException("Pet nao econtrado"));
        pet.setNome(dto.nome());
        pet.setRaca(dto.raca());
        pet.setEspecie(dto.especie());
        pet.setPorte(dto.porte());
        pet.setPeso(dto.peso());
        pet.setSexo(dto.sexo());
        pet.setDataDeNascimento(dto.dataDeNascimento());

        return new PetResponseDTO(pet);
    }


    private String gerarCodigoBruto() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(caracteres.length());
            sb.append(caracteres.charAt(index));
        }
        String codigoVinculo = sb.toString();
        return codigoVinculo.substring(0, 3) + "-" + codigoVinculo.substring(3);

    }
    private String gerarCodigoVinculo(){
        String codigo = gerarCodigoBruto();
        while (petRepository.findByCodigoVinculo(codigo).isPresent()){
            codigo = gerarCodigoBruto();
        }
        return codigo;
    }
    private static final int TAMANHO_MAXIMO_FOTO = 5 * 1024 * 1024;


   }