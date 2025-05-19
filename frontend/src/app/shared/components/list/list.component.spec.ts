import { ComponentFixture, TestBed } from "@angular/core/testing";

import { ListComponent } from "./list.component";

describe("ListComponent", () => {
  let component: ListComponent;
  let fixture: ComponentFixture<ListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should emit itemClickCallback when itemClickHandler is called", () => {
    // Given
    spyOn(component.itemClickCallback, "emit");

    // When
    component.itemClickHandler("anyString");

    // Then
    expect(component.itemClickCallback.emit).toHaveBeenCalledTimes(1);
  });
});
