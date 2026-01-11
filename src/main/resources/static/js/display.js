async function fetchPersonData() {
        try {
            const response = await fetch('http://localhost:8081/api/person');
            const persons = await response.json();

            const tbody = document.getElementById('personTable').querySelector('tbody');
            persons.forEach(person => {
                const row = document.createElement('tr');

                const nameCell = document.createElement('td');
                nameCell.textContent = person.name;
                row.appendChild(nameCell);

                const contactNoCell = document.createElement('td');
                contactNoCell.textContent = person.contactNo;
                row.appendChild(contactNoCell);

                const wingCell = document.createElement('td');
                wingCell.textContent = person.wing;
                row.appendChild(wingCell);

                const flatNoCell = document.createElement('td');
                flatNoCell.textContent = person.flatNo;
                row.appendChild(flatNoCell);

                tbody.appendChild(row);
            });
        } catch (error) {
            console.error('Error fetching person data:', error);
        }
    }

    fetchPersonData();