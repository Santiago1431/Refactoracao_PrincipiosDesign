package projetoprincipiosdesign.entity;

public class Cliente {
    private String nome;
    private Endereco endereco;

    public Cliente(String nome, Endereco endereco) {
        this.nome = nome;
        this.endereco = endereco;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco.getEnderecoCompleto();
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }
}
