//let currentSlide = 0;
//let autoSlideInterval = null;
//
//function showSlide(index) {
//    const slides = document.querySelectorAll('.slide');
//    const totalSlides = slides.length;
//
//    // Wrap-around logic
//    if (index >= totalSlides) {
//        currentSlide = 0; // Loop back to first slide
//    } else if (index < 0) {
//        currentSlide = totalSlides - 1; // Loop to the last slide
//    } else {
//        currentSlide = index;
//    }
//
//    // Update the transform to show the current slide
//    const offset = -currentSlide * 100;
//    document.querySelector('.slides').style.transform = `translateX(${offset}%)`;
//}
//
//function moveSlide(direction) {
//    // Ensure the correct slide is shown by adjusting index
//    showSlide(currentSlide + direction);
//}
//
//function startAutoSlide() {
//    // Clear any existing interval to avoid multiple timers running
//    if (autoSlideInterval) {
//        clearInterval(autoSlideInterval);
//    }
//
//    // Automatically move to the next slide every 3 seconds
//    autoSlideInterval = setInterval(() => {
//        moveSlide(1);  // Move to the next slide
//    }, 3000);  // Change slide every 3 seconds
//}
//
//function stopAutoSlide() {
//    // Clear the interval to stop auto-sliding
//    clearInterval(autoSlideInterval);
//}
//
//document.addEventListener('DOMContentLoaded', () => {
//    // Show the initial slide and start auto-sliding
//    showSlide(currentSlide);
//    startAutoSlide();
//
//    const slider = document.querySelector('.slider');
//
//    // Pause auto-sliding on hover
//    slider.addEventListener('mouseover', stopAutoSlide);
//
//    // Resume auto-sliding when the mouse leaves the slider
//    slider.addEventListener('mouseout', startAutoSlide);
//});







let currentSlide = 0;
let autoSlideInterval = null;

function showSlide(index) {
    const slides = document.querySelectorAll('.slide');
    const totalSlides = slides.length;

    // Wrap-around logic
    if (index >= totalSlides) {
        currentSlide = 0; // Loop back to first slide
    } else if (index < 0) {
        currentSlide = totalSlides - 1; // Loop to the last slide
    } else {
        currentSlide = index;
    }

    // Update the transform to show the current slide
    const offset = -currentSlide * 100;
    document.querySelector('.slides').style.transform = `translateX(${offset}%)`;
}

function moveSlide(direction) {
    // Ensure the correct slide is shown by adjusting index
    showSlide(currentSlide + direction);
}

function startAutoSlide() {
    // Clear any existing interval to avoid multiple timers running
    if (autoSlideInterval) {
        clearInterval(autoSlideInterval);
    }

    // Automatically move to the next slide every 3 seconds
    autoSlideInterval = setInterval(() => {
        moveSlide(1);  // Move to the next slide
    }, 3000);  // Change slide every 3 seconds
}

function stopAutoSlide() {
    // Clear the interval to stop auto-sliding
    clearInterval(autoSlideInterval);
}

document.addEventListener('DOMContentLoaded', () => {
    // Show the initial slide and start auto-sliding
    showSlide(currentSlide);
    startAutoSlide();

    const slider = document.querySelector('.slider');

    // Pause auto-sliding on hover
    slider.addEventListener('mouseover', stopAutoSlide);

    // Resume auto-sliding when the mouse leaves the slider
    slider.addEventListener('mouseout', startAutoSlide);
});

const slides = document.querySelectorAll('.slide');

// Initialize first slide as active
showSlide(currentSlide);

function changeSlide(direction) {
    currentSlide += direction;

    // If currentSlide exceeds the number of slides, loop back
    if (currentSlide >= slides.length) {
        currentSlide = 0;
    }

    // If currentSlide is negative, go to the last slide
    if (currentSlide < 0) {
        currentSlide = slides.length - 1;
    }

    showSlide(currentSlide);
}

function showSlide(index) {
    // Hide all slides
    slides.forEach(slide => {
        slide.style.display = 'none';
    });

    // Show the current slide
    slides[index].style.display = 'block';
}

function toggleDescription(id) {
    const desc = document.getElementById(id);
    if (desc.style.display === "none" || desc.style.display === "") {
        desc.style.display = "block";
    } else {
        desc.style.display = "none";
    }
}