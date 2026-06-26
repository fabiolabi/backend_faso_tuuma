package bf.annuaire.artisans.metier.repository;

import bf.annuaire.artisans.metier.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {}
